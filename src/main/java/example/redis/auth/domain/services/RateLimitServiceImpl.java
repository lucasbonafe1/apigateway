package example.redis.auth.domain.services;

import example.redis.auth.domain.interfaces.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimitServiceImpl implements RateLimitService {
    private final TokenBucketStrategy tokenBucketStrategy;

    public RateLimitServiceImpl(TokenBucketStrategy tokenBucketStrategy) {
        this.tokenBucketStrategy = tokenBucketStrategy;
    }

    public boolean isAllowedCustomKey(String ipKey, int limit, int windowSeconds) {
        return !tokenBucketStrategy.isAllowed("rate_limit:" + ipKey, limit, windowSeconds);
    }

    /**
     * Reseta o contador de uma chave específica.
     *
     * Usado quando uma ação de sucesso deve "perdoar" tentativas
     * anteriores - no nosso caso, um login correto zera o contador
     * de tentativas daquele username, para não penalizar o usuário
     * legítimo com o histórico de erros de digitação antigos.
     */
    @Override
    public void resetCustomKey(String key) {
        tokenBucketStrategy.reset("rate_limit:" + key);

        log.debug("[CustomKey] Reset aplicado em: {}", key);
    }
}
