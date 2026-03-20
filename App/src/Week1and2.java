import java.util.concurrent.*;
import java.util.*;

public class Week1and2 {

    // Token Bucket class
    static class TokenBucket {
        private final int maxTokens;
        private final double refillRatePerMillis;
        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, long refillDurationMillis) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillRatePerMillis = (double) maxTokens / refillDurationMillis;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on time elapsed
        private synchronized void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;

            double tokensToAdd = elapsed * refillRatePerMillis;
            tokens = Math.min(maxTokens, tokens + tokensToAdd);

            lastRefillTime = now;
        }

        // Try consuming 1 token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return (int) tokens;
        }

        public synchronized long getRetryAfterSeconds() {
            if (tokens >= 1) return 0;

            double missingTokens = 1 - tokens;
            return (long) (missingTokens / refillRatePerMillis / 1000);
        }
    }

    // Rate limiter store
    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS = 1000;
    private final long WINDOW_MILLIS = 60 * 60 * 1000; // 1 hour

    // Get or create bucket
    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(clientId,
                k -> new TokenBucket(MAX_REQUESTS, WINDOW_MILLIS));
    }

    // Rate limit check
    public void checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        if (bucket.allowRequest()) {
            System.out.println("Allowed (" + bucket.getRemainingTokens() + " requests remaining)");
        } else {
            long retry = bucket.getRetryAfterSeconds();
            System.out.println("Denied (0 requests remaining, retry after " + retry + "s)");
        }
    }

    // Status API
    public void getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);

        int remaining = bucket.getRemainingTokens();
        int used = MAX_REQUESTS - remaining;

        long resetTime = System.currentTimeMillis() + WINDOW_MILLIS;

        System.out.println("Status for " + clientId + ":");
        System.out.println("{used: " + used +
                ", limit: " + MAX_REQUESTS +
                ", remaining: " + remaining +
                ", reset: " + (resetTime / 1000) + "}");
    }

    // Main method
    public static void main(String[] args) throws InterruptedException {
        Week1and2 rateLimiter = new Week1and2();

        String clientId = "abc123";

        // Simulate requests
        for (int i = 0; i < 1005; i++) {
            rateLimiter.checkRateLimit(clientId);
        }

        // Check status
        rateLimiter.getRateLimitStatus(clientId);
    }
}