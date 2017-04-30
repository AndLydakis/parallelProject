import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/30/17.
 */
public class Bot extends Thread {

    static final ArrayList<statsEntry> attackStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStats = new ArrayList<>();
    static final ArrayList<statsEntry> defendStatsSocket = new ArrayList<>();
    static final ArrayList<statsEntry> attackStatsSocket = new ArrayList<>();

    class statsEntry {
        private int role;
        private int numOps;
        private int avgDelay;

        public statsEntry(int role, int numOps, int avgDelay) {
            this.role = role;
            this.numOps = numOps;
            this.avgDelay = avgDelay;
        }
    }

    public Bot() {
    }

    public void run() {
        while (true) {

        }
    }
}
