package example.redis.auth.domain.interfaces;

public interface RateLimitStrategy {
    boolean isAllowed(String key, int limit, int windowSeconds);

    int getCurrentCount(String key);

    long getTTL(String key);

    void reset(String key);

    String getName();
}