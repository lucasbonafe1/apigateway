package example.redis.auth.domain.interfaces;

public interface RateLimitService {
    boolean isAllowedCustomKey(String key, int limit, int windowSeconds);

    void resetCustomKey(String key);
}
