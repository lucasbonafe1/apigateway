package example.redis.auth.controllers;

import example.redis.auth.models.LoginRequest;
import example.redis.auth.models.LoginResponse;
import example.redis.auth.models.RegisterRequest;
import example.redis.auth.services.interfaces.AuthService;
import example.redis.auth.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RequestUtils requestUtils;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            String clientIp = requestUtils.getClientIp(httpRequest);
            LoginResponse response = authService.login(request, clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao realizar login", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            String clientIp = requestUtils.getClientIp(httpRequest);
            authService.register(request, clientIp);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Erro ao registrar usuário", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}