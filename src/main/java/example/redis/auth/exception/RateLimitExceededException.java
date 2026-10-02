package example.redis.auth.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final int limit;
    private final int remaining;
    private final int retryAfterSeconds;

    public RateLimitExceededException(String message, int limit, int remaining, int retryAfterSeconds) {
        super(message);
        this.limit = limit;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}