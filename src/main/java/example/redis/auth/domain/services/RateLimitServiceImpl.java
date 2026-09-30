package example.redis.auth.domain.services;

import example.redis.auth.domain.interfaces.RateLimitService;
import example.redis.auth.domain.interfaces.RateLimitStrategy;
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
        return tokenBucketStrategy.isAllowed("rate_limit:" + ipKey, limit, windowSeconds);
    }
}
