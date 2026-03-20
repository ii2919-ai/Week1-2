import java.util.*;

public class Week1and2 {

    // Video Data
    static class Video {
        String id;
        String content;

        public Video(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    // LRU Cache using LinkedHashMap
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // Caches
    private LRUCache<String, Video> L1 = new LRUCache<>(10000);
    private LRUCache<String, Video> L2 = new LRUCache<>(100000);

    // Simulated DB (L3)
    private Map<String, Video> database = new HashMap<>();

    // Access count (for promotion logic)
    private Map<String, Integer> accessCount = new HashMap<>();

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;
    private int totalRequests = 0;

    // Initialize DB with sample data
    public Week1and2() {
        for (int i = 1; i <= 200000; i++) {
            database.put("video_" + i, new Video("video_" + i, "Content_" + i));
        }
    }

    // Get Video
    public Video getVideo(String videoId) {
        totalRequests++;

        // L1 lookup
        if (L1.containsKey(videoId)) {
            l1Hits++;
            System.out.println("L1 HIT (0.5ms)");
            return L1.get(videoId);
        }

        System.out.println("L1 MISS");

        // L2 lookup
        if (L2.containsKey(videoId)) {
            l2Hits++;
            System.out.println("L2 HIT (5ms)");

            Video v = L2.get(videoId);

            promoteToL1(videoId, v);
            return v;
        }

        System.out.println("L2 MISS");

        // L3 lookup
        if (database.containsKey(videoId)) {
            l3Hits++;
            System.out.println("L3 HIT (150ms)");

            Video v = database.get(videoId);

            addToL2(videoId, v);
            return v;
        }

        System.out.println("Video not found");
        return null;
    }

    // Promote to L1
    private void promoteToL1(String videoId, Video v) {
        accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);

        if (accessCount.get(videoId) > 2) {
            L1.put(videoId, v);
            System.out.println("Promoted to L1");
        }
    }

    // Add to L2
    private void addToL2(String videoId, Video v) {
        accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
        L2.put(videoId, v);
        System.out.println("Added to L2 (access count: " + accessCount.get(videoId) + ")");
    }

    // Cache Invalidation
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        database.remove(videoId);
        accessCount.remove(videoId);

        System.out.println("Invalidated " + videoId + " from all caches");
    }

    // Statistics
    public void getStatistics() {
        double l1Rate = (l1Hits * 100.0) / totalRequests;
        double l2Rate = (l2Hits * 100.0) / totalRequests;
        double l3Rate = (l3Hits * 100.0) / totalRequests;

        System.out.println("\n===== Cache Statistics =====");
        System.out.printf("L1 Hit Rate: %.2f%% (0.5ms)\n", l1Rate);
        System.out.printf("L2 Hit Rate: %.2f%% (5ms)\n", l2Rate);
        System.out.printf("L3 Hit Rate: %.2f%% (150ms)\n", l3Rate);
        System.out.printf("Overall Hit Rate: %.2f%%\n", (l1Rate + l2Rate + l3Rate));
    }

    // Main method
    public static void main(String[] args) {

        Week1and2 cacheSystem = new Week1and2();

        // First request → L3
        cacheSystem.getVideo("video_123");

        // Second request → L2
        cacheSystem.getVideo("video_123");

        // Third request → L1 (after promotion)
        cacheSystem.getVideo("video_123");

        // Another video
        cacheSystem.getVideo("video_999999"); // not found

        // Invalidate example
        cacheSystem.invalidate("video_123");

        // Stats
        cacheSystem.getStatistics();
    }
}