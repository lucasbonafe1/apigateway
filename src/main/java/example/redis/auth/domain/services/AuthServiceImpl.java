package example.redis.auth.domain.services;

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

    @Value("${base-ip-key}")
    private String ipKey;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        return null;
    }

    @Override
    public void register(RegisterRequest request, String clientIp) {
        try {
            String userIpKey = ipKey + clientIp;

            if (!rateLimitService.isAllowedCustomKey(userIpKey, maxRegisterAttemptsPerIp, registerWindowSeconds)) {
                log.warn("[REGISTER] Rate limit excedido para IP: {}", clientIp);

//                Descomentar quando implementar globalException
//                throw new RateLimitExceededException("Muitas tentativas de cadastro a partir deste IP.", maxRegisterAttemptsPerIp, registerWindowSeconds);
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
