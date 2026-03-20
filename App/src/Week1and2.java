import java.util.*;
import java.util.concurrent.*;

public class Week1and2 {

    // Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        public DNSEntry(String domain, String ipAddress, long ttlMillis) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = this.timestamp + ttlMillis;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity;
    private final Map<String, DNSEntry> cache;

    // Stats
    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;
    private long requestCount = 0;

    // Constructor
    public Week1and2(int capacity) {
        this.capacity = capacity;

        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > Week1and2.this.capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve method
    public synchronized String resolve(String domain, long ttlSeconds) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            recordTime(startTime);
            System.out.println("Cache HIT: " + domain);
            return entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
            System.out.println("Cache EXPIRED: " + domain);
        }

        // Cache miss
        misses++;
        System.out.println("Cache MISS: " + domain);

        String ip = queryUpstreamDNS(domain);

        DNSEntry newEntry = new DNSEntry(domain, ip, ttlSeconds * 1000);
        cache.put(domain, newEntry);

        recordTime(startTime);
        return ip;
    }

    // Simulated upstream DNS query
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "172.217." + (int)(Math.random() * 255) + "." + (int)(Math.random() * 255);
    }

    // Cleanup thread
    private void startCleanupThread() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            synchronized (Week1and2.this) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<String, DNSEntry> entry = it.next();
                    if (entry.getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Stats tracking
    private void recordTime(long startTime) {
        long elapsed = System.nanoTime() - startTime;
        totalLookupTime += elapsed;
        requestCount++;
    }

    public synchronized void getCacheStats() {
        double hitRate = requestCount == 0 ? 0 : (hits * 100.0 / requestCount);
        double avgTimeMs = requestCount == 0 ? 0 : (totalLookupTime / 1_000_000.0 / requestCount);

        System.out.println("\nCache Stats:");
        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
        System.out.println("Avg Lookup Time: " + String.format("%.2f", avgTimeMs) + " ms");
    }

    // Main method
    public static void main(String[] args) throws InterruptedException {
        Week1and2 dnsCache = new Week1and2(3);

        System.out.println(dnsCache.resolve("google.com", 3));
        System.out.println(dnsCache.resolve("google.com", 3));

        Thread.sleep(4000); // wait for TTL expiry

        System.out.println(dnsCache.resolve("google.com", 3));

        dnsCache.getCacheStats();
    }
}