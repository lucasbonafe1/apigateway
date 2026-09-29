package example.redis.auth.services;

import example.redis.auth.models.LoginRequest;
import example.redis.auth.models.LoginResponse;
import example.redis.auth.models.RegisterRequest;
import example.redis.auth.models.User;
import example.redis.auth.repositories.UserRepository;
import example.redis.auth.services.interfaces.AuthService;
import example.redis.auth.services.interfaces.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    private final PasswordEncoder passwordEncoder;

    @Value("${max-register-attempts-per-ip}")
    private int maxRegisterAttemptsPerIp;

    @Value("${register-window-seconds}")
    private int registerWindowSeconds;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        return null;
    }

    @Override
    public void register(RegisterRequest request, String clientIp) {
        try {
            String ipKey = "auth:register:ip:" + clientIp;

            if (!rateLimitService.isAllowedCustomKey(ipKey, maxRegisterAttemptsPerIp, registerWindowSeconds)) {
                log.warn("[REGISTER] Rate limit excedido para IP: {}", clientIp);

                throw new RateLimitExceededException("Muitas tentativas de cadastro a partir deste IP.", maxRegisterAttemptsPerIp, registerWindowSeconds);
            }

            if (userRepository.findByEmail(request.getEmail()) != null) {
                throw new IllegalArgumentException("Email já cadastrado");
            }

            if (userRepository.findByUsername(request.getUsername()) != null) {
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
