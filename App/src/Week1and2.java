import java.util.*;
import java.util.concurrent.*;

public class Week1and2 {

    // Page view count
    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();

    // Unique visitors per page
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source count
    private Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Event processing
    public void processEvent(String url, String userId, String source) {

        // Count page views
        pageViews.merge(url, 1, Integer::sum);

        // Track unique visitors
        uniqueVisitors
                .computeIfAbsent(url, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        // Count traffic sources
        trafficSources.merge(source, 1, Integer::sum);
    }

    // Get top N pages
    private List<Map.Entry<String, Integer>> getTopPages(int n) {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > n) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue()); // descending
        return result;
    }

    // Dashboard output
    public void getDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        // Top pages
        System.out.println("Top Pages:");
        List<Map.Entry<String, Integer>> topPages = getTopPages(10);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();

            System.out.println(rank++ + ". " + url +
                    " - " + views + " views (" + unique + " unique)");
        }

        // Traffic sources
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("================================\n");
    }

    // Start dashboard auto-refresh every 5 seconds
    public void startDashboard() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::getDashboard,
                5, 5, TimeUnit.SECONDS);
    }

    // Main method
    public static void main(String[] args) throws InterruptedException {
        Week1and2 analytics = new Week1and2();

        // Start dashboard updates
        analytics.startDashboard();

        // Simulate incoming events
        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-trends",
                "/health/wellness"
        };

        String[] sources = {"google", "facebook", "direct", "twitter"};

        Random rand = new Random();

        // Simulate high traffic stream
        for (int i = 0; i < 1000; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String userId = "user_" + rand.nextInt(500);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(url, userId, source);

            Thread.sleep(10); // simulate real-time stream
        }

        // Keep app running
        Thread.sleep(20000);
    }
}