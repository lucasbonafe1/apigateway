package example.redis.auth.services;

import example.redis.auth.services.interfaces.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimitServiceImpl implements RateLimitService {
    public boolean isAllowedCustomKey(String key, int limit, int windowSeconds) {
        //continuar daqui com estrategia de rate limit e TokenBucket
        return true;
    }
}
