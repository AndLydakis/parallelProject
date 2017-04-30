import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/30/17.
 */
public class RmiBot extends Bot {
    private RemoteState state;
    private int role;
    private int numOps;
    private int avgDelay;
    private long sleep;
    String username;
    private String targets;
    private volatile boolean running;

    private void addStats() {
        if (role == 1) {
            synchronized (attackStats) {
                attackStats.add(new statsEntry(role, numOps, avgDelay));
            }
        } else {
            synchronized (defendStats) {
                defendStats.add(new statsEntry(role, numOps, avgDelay));
            }
        }
    }

    public void selectAttack() throws RemoteException {
        if (targets == null) {
            running = false;
            return;
        }
        String[] tokens = targets.split("\n");
        int res = state.requestPrimary(username, role, tokens[0]);
        if (res < 0) running = false;
    }

    public RmiBot(RemoteState s, String username, int role, long sleep) {
        running = true;
        this.state = s;
        this.username = username;
        this.role = role;
        this.sleep = sleep;
        if (role == 1) {
            System.err.println("Created new RMI attacker bot");
        } else {
            System.err.println("Created new RMI defender bot");
        }
    }

    public void run() {
        long start;
        try {
            if (!state.register(username, role)) {
                return;
            }
        }catch (RemoteException re){
            re.printStackTrace();
            return;
        }
        try {
            while (state.isAlive() && running) {
//            while ((targets = state.getTargets()) != null && running) {
                if (targets == null) {
                    start = System.nanoTime();
                    targets = state.getTargets();
                    avgDelay += (System.nanoTime() - start);
                    numOps++;
                    continue;
                }
                start = System.nanoTime();
                selectAttack();
                avgDelay += (System.nanoTime() - start);
                numOps++;

            }
            avgDelay /= numOps;
            addStats();

        } catch (Exception e) {
            e.printStackTrace();
            if (numOps != 0) {
                avgDelay /= numOps;
                addStats();
            }
        }
    }
}
