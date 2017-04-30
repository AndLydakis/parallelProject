import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by lydakis-local on 4/30/17.
 */
public class RmiBot extends Bot {
    private RemoteState state;
    private int role;
    private int numOps;
    private int primary;
    private int secondary;
    private int items;
    private int avgDelay;
    private long sleep;
    String username;
    private String regString;
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
            System.err.println("could not find targets");
            running = false;
            return;
        }
        String[] tokens = targets.split("\n");
//        System.err.println("RMI " + roles[role] + " " + username + " targeting " + tokens[0].split(":")[0]);
        int res = state.requestPrimary(username, role, tokens[0].split(":")[0]);
        if (res < 0) {
            System.err.println(roles[role] + " " + username + " Could not contact server");
            running = false;
        }
    }

    public RmiBot(RemoteState s, String username, int role, long sleep, String regString) {
        this.running = true;
        this.state = s;
        this.username = username;
        this.role = role;
        this.sleep = sleep;
        this.regString = regString;
        if(!Objects.equals(regString, "")) {
            System.err.println(regString);
            String[] tokens = regString.split("-");
            this.primary = Integer.parseInt(tokens[2]);
            this.secondary = Integer.parseInt(tokens[3]);
            this.items = Integer.parseInt(tokens[4]);
        }else{
            this.primary = -1;
        }
    }

    public void run() {
        long start;
        System.err.println("Trying to register " + roles[role] + " " + username);
        try {
            if(primary!=-1){
                if (!state.register(username, role, 0, 0, primary, secondary, items)) {
                    System.err.println("Registration failed");
                    return;
                }
            }else {
                if (!state.register(username, role)) {
                    System.err.println("Registration failed");
                    return;
                }
            }
        } catch (RemoteException re) {
            re.printStackTrace();
            return;
        }
        if (role == 1) {
            System.err.println("Created new RMI attacker bot: " + username);
        } else {
            System.err.println("Created new RMI defender bot: " + username);
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
                start = System.nanoTime();
                while((System.nanoTime() - start)>sleep){}

            }
            avgDelay /= numOps;
            System.err.println(username + " adding stats");
            addStats();

        } catch (Exception e) {
            e.printStackTrace();
            if (numOps != 0) {
                avgDelay /= numOps;
                System.err.println(username + " adding stats");
                addStats();
            }
        }
    }
}
