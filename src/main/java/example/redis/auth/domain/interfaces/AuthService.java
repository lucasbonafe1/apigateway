package example.redis.auth.domain.interfaces;

import example.redis.auth.model.LoginRequest;
import example.redis.auth.model.LoginResponse;
import example.redis.auth.model.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, String clientIp);
    void register(RegisterRequest request, String clientIp);
}
