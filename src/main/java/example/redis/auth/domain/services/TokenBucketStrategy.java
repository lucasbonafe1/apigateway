package example.redis.auth.domain.services;

import example.redis.auth.domain.interfaces.RateLimitStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class TokenBucketStrategy implements RateLimitStrategy {
    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBucketStrategy(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        // No Redis, vamos guardar DUAS informações para cada "balde":
        // 1) quantas fichas tem sobrando agora
        // 2) em que momento (timestamp) checamos isso pela última vez
        String tokensKey = key + ":tokens";
        String timestampKey = key + ":last_check";

        double currentTokens = getStoredTokens(tokensKey, limit);
        long lastCheck = getStoredTimestamp(timestampKey);
        long now = Instant.now().getEpochSecond();

        // PASSO 1: calcular quanto tempo passou desde a última checagem
        long elapsedSeconds = now - lastCheck;

        // PASSO 2: calcular a "velocidade de recarga" do balde
        // Se o limite é 5 fichas a cada 900 segundos,
        // a cada 1 segundo o balde recupera 5/900 ≈ 0.0056 fichas
        double refillRate = (double) limit / windowSeconds;

        // PASSO 3: calcular quantas fichas foram recarregadas nesse intervalo
        double tokensRecovered = elapsedSeconds * refillRate;

        // PASSO 4: somar ao que já tinha, sem nunca ultrapassar o limite máximo
        double availableTokens = Math.min(currentTokens + tokensRecovered, limit);

        log.debug("[TokenBucket] key={} | tinha={} | recuperou={} | disponível={}",
                key, currentTokens, tokensRecovered, availableTokens
        );

        // PASSO 5: decidir se permite a requisição
        if (availableTokens < 1.0) {
            // Não tem nem 1 ficha inteira disponível -> bloqueia
            // Importante: mesmo bloqueando, salvamos o estado atualizado,
            // para a próxima tentativa continuar de onde parou
            saveState(tokensKey, timestampKey, availableTokens, now, windowSeconds);
            return false;
        }

        // Tem ficha disponível -> consome 1 e permite
        double remainingAfterConsume = availableTokens - 1.0;
        saveState(tokensKey, timestampKey, remainingAfterConsume, now, windowSeconds);
        return true;
    }

    /**
     * Salva o estado atualizado do balde no Redis.
     *
     * Usamos expire() com base na janela de tempo: se ninguém usar essa
     * chave por muito tempo, o Redis limpa sozinho - não ficamos acumulando lixo de usuários inativos.
     */
    private void saveState(String tokensKey, String timestampKey, double tokens, long timestamp, int windowSeconds) {
        redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens));
        redisTemplate.opsForValue().set(timestampKey, String.valueOf(timestamp));

        redisTemplate.expire(tokensKey, java.time.Duration.ofSeconds(windowSeconds));
        redisTemplate.expire(timestampKey, java.time.Duration.ofSeconds(windowSeconds));
    }

    /**
     * Busca no Redis quantas fichas estão salvas.
     * Se a chave não existe (primeira vez), o balde começa CHEIO.
     */
    private double getStoredTokens(String tokensKey, int limit) {
        Object stored = redisTemplate.opsForValue().get(tokensKey);
        if (stored == null) {
            return limit; // balde começa cheio
        }
        return Double.parseDouble(stored.toString());
    }

    /**
     * Busca o timestamp da última checagem.
     * Se não existe ainda, consideramos "agora" (não perdeu tempo nenhum).
     */
    private long getStoredTimestamp(String timestampKey) {
        Object stored = redisTemplate.opsForValue().get(timestampKey);
        if (stored == null) {
            return Instant.now().getEpochSecond();
        }
        return Long.parseLong(stored.toString());
    }

    @Override
    public int getCurrentCount(String key) {
        return (int) getStoredTokens(key + ":tokens", 0);
    }

    @Override
    public long getTTL(String key) {
        Long ttl = redisTemplate.getExpire(key + ":tokens");
        return ttl != null ? ttl : -1;
    }

    @Override
    public void reset(String key) {
        redisTemplate.delete(key + ":tokens");
        redisTemplate.delete(key + ":last_refill");
    }

    @Override
    public String getName() {
        return "TOKEN_BUCKET";
    }
}
