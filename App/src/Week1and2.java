import java.util.*;

/**
 * Week1and2 simulates a Social Media Username Availability Checker.
 * It demonstrates O(1) username lookup, frequency counting, and
 * suggestions for alternative usernames if a requested one is taken.
 *
 * Author: Student
 * Version: 1.0
 */
public class Week1and2 {

    // Stores registered usernames for O(1) lookup
    private HashMap<String, Integer> usernameMap;

    // Tracks frequency of attempted username checks
    private HashMap<String, Integer> attemptFrequency;

    // Simulated userId counter
    private int userIdCounter;

    public Week1and2() {
        usernameMap = new HashMap<>();
        attemptFrequency = new HashMap<>();
        userIdCounter = 1;

        // Pre-populate with some usernames
        registerUsername("john_doe");
        registerUsername("admin");
        registerUsername("user123");
    }

    /**
     * Check if username is available.
     * @param username username to check
     * @return true if available, false if taken
     */
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameMap.containsKey(username);
    }

    /**
     * Register a new username if available.
     */
    public boolean registerUsername(String username) {
        if (checkAvailability(username)) {
            usernameMap.put(username, userIdCounter++);
            return true;
        }
        return false;
    }

    /**
     * Suggest alternative usernames if taken.
     */
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int suffix = 1;

        while (suggestions.size() < 3) {
            String alternative = username + suffix;
            if (checkAvailability(alternative)) {
                suggestions.add(alternative);
            }
            suffix++;
        }

        // Optional: add a dot-separated version
        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (checkAvailability(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        return suggestions;
    }

    /**
     * Get the username attempted most frequently.
     */
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostAttempted = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return mostAttempted + " (" + maxCount + " attempts)";
    }

    // Sample driver method
    public static void main(String[] args) {
        Week1and2 checker = new Week1and2();

        System.out.println("Check 'john_doe': " + checker.checkAvailability("john_doe")); // false
        System.out.println("Check 'jane_smith': " + checker.checkAvailability("jane_smith")); // true

        System.out.println("Register 'jane_smith': " + checker.registerUsername("jane_smith")); // true
        System.out.println("Register 'john_doe': " + checker.registerUsername("john_doe")); // false

        System.out.println("Suggestions for 'john_doe': " + checker.suggestAlternatives("john_doe"));
        System.out.println("Most attempted username: " + checker.getMostAttempted());
    }
}