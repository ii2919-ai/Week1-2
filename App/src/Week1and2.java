import java.util.*;

public class Week1and2 {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> frequencyMap = new HashMap<>(); // query → frequency
    }

    private TrieNode root = new TrieNode();

    // Global frequency map
    private Map<String, Integer> globalFrequency = new HashMap<>();

    private static final int TOP_K = 10;

    // Insert query into Trie
    public void insert(String query) {
        globalFrequency.put(query, globalFrequency.getOrDefault(query, 0) + 1);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // Update frequency at each prefix node
            node.frequencyMap.put(query, globalFrequency.get(query));
        }
    }

    // Get top K suggestions for prefix
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                // Typo handling: fallback to shorter prefix
                return fallbackSearch(prefix);
            }
            node = node.children.get(c);
        }

        return getTopK(node.frequencyMap);
    }

    // Fallback for typo tolerance (remove last char)
    private List<String> fallbackSearch(String prefix) {
        if (prefix.length() <= 1) return new ArrayList<>();

        return search(prefix.substring(0, prefix.length() - 1));
    }

    // Get top K using Min Heap
    private List<String> getTopK(Map<String, Integer> freqMap) {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > TOP_K) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue());

        List<String> suggestions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : result) {
            suggestions.add(entry.getKey() + " (" + entry.getValue() + ")");
        }

        return suggestions;
    }

    // Update frequency (new search)
    public void updateFrequency(String query) {
        insert(query);
    }

    // Main method
    public static void main(String[] args) {

        Week1and2 system = new Week1and2();

        // Insert sample queries
        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java 21 features");

        // Search
        System.out.println("Search results for 'jav':");
        List<String> results = system.search("jav");

        int rank = 1;
        for (String res : results) {
            System.out.println(rank++ + ". " + res);
        }

        // Update frequency
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nAfter updating frequency:");
        results = system.search("jav");

        rank = 1;
        for (String res : results) {
            System.out.println(rank++ + ". " + res);
        }
    }
}