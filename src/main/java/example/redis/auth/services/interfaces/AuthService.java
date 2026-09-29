package example.redis.auth.services.interfaces;

import example.redis.auth.models.LoginRequest;
import example.redis.auth.models.LoginResponse;
import example.redis.auth.models.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, String clientIp);
    void register(RegisterRequest request, String clientIp);
}
