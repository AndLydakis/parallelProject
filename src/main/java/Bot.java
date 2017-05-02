import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic bot interface
 */
public class Bot extends Thread {

    static final ArrayList<statsEntry> attackStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStatsSocket = new ArrayList<>();
    static final ArrayList<statsEntry> attackStatsSocket = new ArrayList<>();
    static final String[] roles = {"Defender", "Attacker"};
    static AtomicInteger counter = new AtomicInteger(0);

    /**
     * class used to keep track of a bot's stats
     */
    class statsEntry {
        private int role;
        private int numOps;
        private int avgDelay;

        statsEntry(int role, int numOps, int avgDelay) {
            this.role = role;
            this.numOps = numOps;
            this.avgDelay = avgDelay;
        }

        public String toString() {
            return this.role + " " + this.numOps + " " + this.avgDelay / 1e9 + "\n";
        }
    }

    Bot() {
    }
}
