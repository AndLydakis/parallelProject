import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic bot interface
 */
public class Bot extends Thread {

    static final ArrayList<statsEntry> attackStatsRmi = new ArrayList<>();
    static final ArrayList<statsEntry> defendStatsRmi = new ArrayList<>();
    static final ArrayList<statsEntry> defendStatsSocket = new ArrayList<>();
    static final ArrayList<statsEntry> attackStatsSocket = new ArrayList<>();
    static final String[] roles = {"Defender", "Attacker"};
    static AtomicInteger counter = new AtomicInteger(0);

    CountDownLatch countDownLatch;
    String username;
    String regString;
    String targets;
    volatile boolean running;
    int role;
    int numOps;
    int primary;
    int secondary;
    int items;
    long avgDelay;
    long sleep;

    /**
     * Add the stats to the correct array
     */
    void addStats() {
        System.err.println(username + " adding stats");
        if (role == 1) {
            synchronized (attackStatsRmi) {
                attackStatsSocket.add(new statsEntry(role, numOps, avgDelay));
            }
        } else {
            synchronized (defendStatsRmi) {
                defendStatsSocket.add(new statsEntry(role, numOps, avgDelay));
            }
        }
    }

    /**
     * class used to keep track of a bot's stats
     */
    class statsEntry {
        private int role;
        private int numOps;
        private long avgDelay;

        /**
         * @param role     Attacker = 1, Defender = 0
         * @param numOps   Number of actions bot performed
         * @param avgDelay in nanoseconds
         */
        statsEntry(int role, int numOps, long avgDelay) {
            this.role = role;
            this.numOps = numOps;
            this.avgDelay = avgDelay;
        }

        public String toString() {
            return this.role + " " + this.numOps + " " + this.avgDelay + "\n";
        }
    }

    Bot() {
    }
}
