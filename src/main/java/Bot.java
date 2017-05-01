import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lydakis-local on 4/30/17.
 */
public class Bot extends Thread {

    static final ArrayList<statsEntry> attackStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStatsSocket = new ArrayList<>();
    static final ArrayList<statsEntry> attackStatsSocket = new ArrayList<>();
    static final String[] roles = {"Defender", "Attacker"};
    static AtomicInteger counter = new AtomicInteger(0);

    class statsEntry {
        private int role;
        private int numOps;
        private int avgDelay;

        public statsEntry(int role, int numOps, int avgDelay) {
            this.role = role;
            this.numOps = numOps;
            this.avgDelay = avgDelay;
        }

        public String toString() {
            return this.role + " " + this.numOps + " " + this.avgDelay / 1e9 + "\n";
        }
    }

    public Bot() {
    }

    public void run() {
        while (true) {

        }
    }
}
