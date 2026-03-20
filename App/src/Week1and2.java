import java.util.*;

public class Week1and2 {

    // Transaction class
    static class Transaction {
        int id;
        int amount;
        String merchant;
        long time; // epoch millis
        String account;

        public Transaction(int id, int amount, String merchant, long time, String account) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.time = time;
            this.account = account;
        }

        public String toString() {
            return "id:" + id + " amt:" + amount;
        }
    }

    // ------------------ 1. Classic Two-Sum ------------------
    public static List<String> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add("(" + map.get(complement) + ", " + t + ")");
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // ------------------ 2. Two-Sum with Time Window ------------------
    public static List<String> findTwoSumWithTimeWindow(List<Transaction> transactions,
                                                        int target,
                                                        long windowMillis) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Map<Integer, Transaction> map = new HashMap<>();

            for (int j = i; j < transactions.size(); j++) {
                Transaction t1 = transactions.get(j);

                if (t1.time - transactions.get(i).time > windowMillis) break;

                int complement = target - t1.amount;

                if (map.containsKey(complement)) {
                    result.add("(" + map.get(complement) + ", " + t1 + ")");
                }

                map.put(t1.amount, t1);
            }
        }

        return result;
    }

    // ------------------ 3. K-Sum ------------------
    public static List<List<Transaction>> findKSum(List<Transaction> transactions,
                                                   int k,
                                                   int target) {
        List<List<Transaction>> result = new ArrayList<>();
        kSumHelper(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void kSumHelper(List<Transaction> transactions,
                                   int k,
                                   int target,
                                   int start,
                                   List<Transaction> current,
                                   List<List<Transaction>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k == 0 || start >= transactions.size()) return;

        for (int i = start; i < transactions.size(); i++) {
            current.add(transactions.get(i));

            kSumHelper(transactions,
                    k - 1,
                    target - transactions.get(i).amount,
                    i + 1,
                    current,
                    result);

            current.remove(current.size() - 1);
        }
    }

    // ------------------ 4. Duplicate Detection ------------------
    public static List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "-" + t.merchant;

            map.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate: " + entry.getKey() +
                        " accounts=" + entry.getValue());
            }
        }

        return result;
    }

    // ------------------ Main Method ------------------
    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();

        long baseTime = System.currentTimeMillis();

        transactions.add(new Transaction(1, 500, "StoreA", baseTime, "acc1"));
        transactions.add(new Transaction(2, 300, "StoreB", baseTime + 1000, "acc2"));
        transactions.add(new Transaction(3, 200, "StoreC", baseTime + 2000, "acc3"));
        transactions.add(new Transaction(4, 500, "StoreA", baseTime + 3000, "acc4")); // duplicate

        // 1. Classic Two-Sum
        System.out.println("Two-Sum:");
        System.out.println(findTwoSum(transactions, 500));

        // 2. Two-Sum with Time Window (2 seconds)
        System.out.println("\nTwo-Sum with Time Window:");
        System.out.println(findTwoSumWithTimeWindow(transactions, 500, 2000));

        // 3. K-Sum (k=3)
        System.out.println("\nK-Sum (k=3):");
        List<List<Transaction>> ksum = findKSum(transactions, 3, 1000);
        for (List<Transaction> list : ksum) {
            System.out.println(list);
        }

        // 4. Duplicate Detection
        System.out.println("\nDuplicates:");
        System.out.println(detectDuplicates(transactions));
    }
}