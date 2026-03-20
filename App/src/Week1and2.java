import java.util.*;

public class Week1and2 {

    // Spot status
    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    // Parking Spot class
    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        public ParkingSpot() {
            this.status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity;
    private int size = 0;

    // Stats
    private int totalProbes = 0;
    private int totalRequests = 0;
    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public Week1and2(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public void parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        totalRequests++;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        totalProbes += probes;

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        size++;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

        System.out.println("Parked " + licensePlate +
                " → Spot #" + index + " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status != Status.EMPTY) {
            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(licensePlate)) {

                long durationMillis = System.currentTimeMillis() - table[index].entryTime;
                double hours = durationMillis / (1000.0 * 60 * 60);

                double fee = hours * 5; // $5 per hour

                table[index].status = Status.DELETED;
                size--;

                System.out.printf("Exit %s → Spot #%d freed, Duration: %.2f hrs, Fee: $%.2f\n",
                        licensePlate, index, hours, fee);
                return;
            }

            index = (index + 1) % capacity;
            probes++;
        }

        System.out.println("Vehicle not found: " + licensePlate);
    }

    // Find nearest available spot (from entrance = index 0)
    public int findNearestSpot() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status != Status.OCCUPIED) {
                return i;
            }
        }
        return -1;
    }

    // Statistics
    public void getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = totalRequests == 0 ? 0 : (totalProbes * 1.0 / totalRequests);

        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyTraffic.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        System.out.println("\n=== Parking Statistics ===");
        System.out.printf("Occupancy: %.2f%%\n", occupancy);
        System.out.printf("Avg Probes: %.2f\n", avgProbes);
        System.out.println("Peak Hour: " + peakHour + ":00 - " + (peakHour + 1) + ":00");
    }

    // Main method
    public static void main(String[] args) throws InterruptedException {
        Week1and2 parking = new Week1and2(10);

        parking.parkVehicle("ABC-1234");
        parking.parkVehicle("ABC-1235");
        parking.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        parking.exitVehicle("ABC-1234");

        System.out.println("Nearest Available Spot: #" + parking.findNearestSpot());

        parking.getStatistics();
    }
}