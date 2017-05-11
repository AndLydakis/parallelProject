import java.rmi.RemoteException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A bot that uses the RMI interface to target
 * the first available block until the game is over
 */
public class RmiBot extends Bot {
    private RemoteState state;
    private boolean randomTargeting;

    /**
     * Add the stats to the correct array
     */
    void addStats() {
        System.err.println(username + " adding stats");
        if (role == 1) {
            synchronized (attackStatsRmi) {
                attackStatsRmi.add(new statsEntry(role, numOps, avgDelay));
            }
        } else {
            synchronized (defendStatsRmi) {
                defendStatsRmi.add(new statsEntry(role, numOps, avgDelay));
            }
        }
    }

    /**
     * Select first available block and use primary on it
     * if there are no available targets do nothing
     *
     * @throws RemoteException if rmi fails
     */
    private void selectAttack() throws RemoteException {
        if (targets == null || targets.isEmpty()) return;
        String[] tokens = targets.split("\n");
//        System.err.println("RMI " + roles[role] + " " + username + " targeting " + tokens[0].split(":")[0]);

        int block = randomTargeting ? ThreadLocalRandom.current().nextInt(0, tokens.length) : 0;

        int res = state.requestPrimary(username, role, tokens[block].split(":")[0]);
        if (res < 0) {
            long start = System.nanoTime();
            targets = state.getTargets();
            avgDelay += (System.nanoTime() - start);
            numOps++;
        }
    }

    /**
     * Constructor
     *
     * @param s         game state
     * @param username  player name
     * @param role      player role
     * @param sleep     time to sleep between attacks(nanoseconds)
     * @param regString string to pass to the registration function
     */
    RmiBot(RemoteState s, String username, int role, long sleep, String regString, CountDownLatch countDownLatch, Boolean randomTargeting) {
        this.running = true;
        this.state = s;
        this.username = username;
        this.role = role;
        this.sleep = sleep;
        this.regString = regString;
        if (!Objects.equals(regString, "")) {
            System.err.println(regString);
            String[] tokens = regString.split("-");
            this.primary = Integer.parseInt(tokens[2]);
            this.secondary = Integer.parseInt(tokens[3]);
            this.items = Integer.parseInt(tokens[4]);
        } else {
            this.primary = -1;
        }

        this.countDownLatch = countDownLatch;
        this.randomTargeting = randomTargeting;
    }

    public void run() {
        long start;
        System.err.println("Trying to register " + roles[role] + " " + username);
        try {
            if (primary != -1) {
                if (!state.register(username, role, 0, 0, primary, secondary, items)) {
                    System.err.println("Registration failed");
                    return;
                }
            } else {
                if (!state.register(username, role)) {
                    System.err.println("Registration failed");
                    return;
                }
            }
        } catch (RemoteException re) {
            System.err.println(username + " Exception 1 ");
            re.printStackTrace();
            running = false;
            return;
        }

        System.err.println("Created new RMI " + (role == 1 ? "attacker" : "defender") + " bot #" + counter.incrementAndGet() + ": " + username);

        try {
            countDownLatch.countDown();
            countDownLatch.await();
//            while (state.printStatus() != 0) {
            while (state.isAlive()) {
//            while (state.isAlive() && running) {
//            while ((targets = state.getTargets()) != null && running) {
                try {
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

//                    start = System.nanoTime();
//                    while ((System.nanoTime() - start) > sleep) {
//                    }

                    if (sleep > 0) Thread.sleep((long) (sleep * 1e6));

                } catch (Exception e) {
//                    System.err.println(username + " inner Exception");
                }
            }
            System.err.println("Game over");
            if (numOps != 0) {
                avgDelay /= numOps;
                addStats();
            } else {
                System.err.println(username + " no ops performed");
            }
        } catch (Exception e) {
            System.err.println(username + " exception 2:");
            e.printStackTrace();
            running = false;
            if (numOps != 0) {
                avgDelay /= numOps;
                addStats();
            } else {
                System.err.println(username + " no ops performed");
            }
        }
    }
}
