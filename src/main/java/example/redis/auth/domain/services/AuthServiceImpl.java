package example.redis.auth.domain.services;

import example.redis.auth.exception.RateLimitExceededException;
import example.redis.auth.model.LoginRequest;
import example.redis.auth.model.LoginResponse;
import example.redis.auth.model.RegisterRequest;
import example.redis.auth.model.User;
import example.redis.auth.repository.UserRepository;
import example.redis.auth.domain.interfaces.AuthService;
import example.redis.auth.domain.interfaces.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    private final PasswordEncoder passwordEncoder;

    @Value("${max-login-attempts-per-username}")
    private int maxLoginAttemptsPerUsername;

    @Value("${login-window-seconds}")
    private int loginWindowSeconds;

    @Value("${max-register-attempts-per-ip}")
    private int maxRegisterAttemptsPerIp;

    @Value("${register-window-seconds}")
    private int registerWindowSeconds;

    @Value("${base-ip-key}")
    private String ipKey;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {

        String userKey = "auth:login:user:" + request.getUsername();
        String ipKey = "auth:login:ip:" + clientIp;

        // 1. Rate limit por CONTA - protege contra brute force numa senha específica
        if (rateLimitService.isAllowedCustomKey(userKey, maxLoginAttemptsPerUsername, loginWindowSeconds)) {
            log.warn("[LOGIN] Rate limit excedido para username: {}", request.getUsername());
            throw new RateLimitExceededException(
                    "Muitas tentativas para este usuário. Tente novamente mais tarde.",
                    maxLoginAttemptsPerUsername, 0, loginWindowSeconds
            );
        }

        // 2. Rate limit por IP - protege contra credential stuffing (testar várias contas)
        if (rateLimitService.isAllowedCustomKey(ipKey, maxLoginAttemptsPerUsername, loginWindowSeconds)) {
            log.warn("[LOGIN] Rate limit excedido para IP: {}", clientIp);
            throw new RateLimitExceededException(
                    "Muitas tentativas a partir deste IP. Tente novamente mais tarde.",
                    maxLoginAttemptsPerUsername, 0, loginWindowSeconds
            );
        }

        // 3. Buscar usuário no banco
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[LOGIN] Usuário não encontrado: {}", request.getUsername());
                    return new BadCredentialsException("Usuário ou senha inválidos");
                });

        // 4. Validar senha (compara texto puro digitado com o hash salvo)
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[LOGIN] Senha inválida para: {}", request.getUsername());
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        // 5. Sucesso -> reseta o contador do username
        // (usuário legítimo não deve "herdar" tentativas erradas antigas)
        rateLimitService.resetCustomKey(userKey);

        log.info("[LOGIN] Sucesso para: {}", request.getUsername());

        // 6. Por enquanto sem JWT - retornamos um placeholder
        return LoginResponse.builder()
                .accessToken("TOKEN_PLACEHOLDER")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }

    @Override
    public void register(RegisterRequest request, String clientIp) {
        try {
            String userIpKey = ipKey + clientIp;

            if (rateLimitService.isAllowedCustomKey(userIpKey, maxRegisterAttemptsPerIp, registerWindowSeconds)) {
                log.warn("[REGISTER] Rate limit excedido para IP: {}", clientIp);

                throw new RateLimitExceededException("Muitas tentativas de cadastro a partir deste IP.", maxRegisterAttemptsPerIp, registerWindowSeconds, 0);
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username já cadastrado");
            }

            String hashedPassword = passwordEncoder.encode(request.getPassword());

            User newUser = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(hashedPassword)
                    .build();

            userRepository.save(newUser);

            log.info("[REGISTER] Novo usuário criado: {}", request.getUsername());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
