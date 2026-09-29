package example.redis.auth.services.interfaces;

public interface RateLimitService {
    boolean isAllowedCustomKey(String key, int limit, int windowSeconds);
}
