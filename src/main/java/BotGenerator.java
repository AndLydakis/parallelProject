import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Creates rmi and socket attacker and defender bots that target
 * the first block on their list until the game is over
 */
public class BotGenerator {

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    /**
     * @param sleep sleep in seconds
     * @return sleep in nanoseconds +/- 5%, but no less than zero
     */
    private long randomizedSleep(long sleep) {
//        return (long) Math.max(0, (sleep * 1e9 * (1 + (Math.random() - .5) / 10)));
        return (long) (sleep * 1e9);
    }

    /**
     * @param host
     * @param port
     * @param RMIA              # of RMI Attackers
     * @param RMID              # of RMI Defenders
     * @param SocketA           # of Socket Attackers
     * @param SocketD           # of Socket Defenders
     * @param sleep             time in seconds between bot actions
     * @param attackerPrimary   Attacker attack damage
     * @param attackerSecondary TODO
     * @param attackerItems     TODO
     * @param defenderPrimary   Defender repair heal amount
     * @param defenderSecondary TODO
     * @param defenderItems     TODO
     * @throws InterruptedException
     * @throws IOException
     */
    private BotGenerator(String host, int port,
                         int RMIA, int RMID,
                         int SocketA, int SocketD,
                         long sleep,
                         int attackerPrimary, int attackerSecondary, int attackerItems,
                         int defenderPrimary, int defenderSecondary, int defenderItems) throws InterruptedException, IOException {
        String defString = defenderPrimary + "-" + defenderSecondary + "-" + defenderItems;
        String atkString = attackerPrimary + "-" + attackerSecondary + "-" + attackerItems;

        System.setProperty("java.rmi.server.hostname", host);
        System.err.println("java.rmi.server.hostname: " + System.getProperty("java.rmi.server.hostname"));

        String service = "rmi://" + host + ":" + port + "/" + GameServer.SERVER_NAME;
        RemoteState state = null;
        int num = RMIA + RMID + SocketA + SocketD;

        try {
            state = (RemoteState) java.rmi.Naming.lookup(service);
        } catch (Exception e) {
            System.err.println("Could not connect to server, we apologize for the inconvenience");
            System.exit(0);
        }
        System.err.println("Host: " + host);
        System.err.println("Port: " + port);
        System.err.println("RMIAttackers: " + RMIA);
        System.err.println("RMIDefenders: " + RMID);
        System.err.println("SocketAttackers: " + SocketA);
        System.err.println("SocketDefenders: " + SocketD);
        System.err.println("Sleep(seconds): " + sleep);
        ArrayList<Bot> bots = new ArrayList<>();

        CountDownLatch countDownLatch = new CountDownLatch(num + 1);

        for (int i = 0; i < RMIA; i++) {
            String username = "RMIAttacker:" + getSaltString();
            String regString = username + "-" + 1 + "-" + atkString;
            bots.add(new RmiBot(state, username, 1, randomizedSleep(sleep), regString, countDownLatch));
        }

        for (int i = 0; i < RMID; i++) {
            String username = "RMIDefender:" + getSaltString();
            String regString = username + "-" + 0 + "-" + defString;
            bots.add(new RmiBot(state, username, 0, randomizedSleep(sleep), regString, countDownLatch));
        }

        for (int i = 0; i < SocketA; i++) {
            String username = "SocketAttacker:" + getSaltString();
            String regString = username + "-" + 1 + "-" + atkString;
            bots.add(new SocketBot(username, 1, host, port, randomizedSleep(sleep), regString, countDownLatch));
        }

        for (int i = 0; i < SocketD; i++) {
            String username = "SocketDefender:" + getSaltString();
            String regString = username + "-" + 0 + "-" + defString;
            bots.add(new SocketBot(username, 0, host, port, randomizedSleep(sleep), regString, countDownLatch));
        }

        System.err.println((num + 1) + "?: " + countDownLatch.getCount());
        bots.forEach(Thread::start);

        System.err.println(num + "?: " + countDownLatch.getCount());
        countDownLatch.countDown();
        countDownLatch.await();
        System.err.println("0?: " + countDownLatch.getCount());

        for (Bot b : bots) {
            b.join();
        }

        System.err.println("Saving stats");

        //LOG RMI BOTS
        BufferedWriter outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/RMISTATS_" + num + "_" +
                        RMIA + "_" + RMID + "_" + SocketA + "_" + SocketD +
                        ".csv"));

        for (Bot.statsEntry se : Bot.attackStats)
            outputWriter.write(se.toString());

        for (Bot.statsEntry se : Bot.defendStats)
            outputWriter.write(se.toString());

        outputWriter.close();

        //LOG SOCKETBOTS
        outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/SOCKETSTATS_" + num + "_" +
                        RMIA + "_" + RMID + "_" + SocketA + "_" + SocketD +
                        ".csv"));

        for (Bot.statsEntry se : Bot.attackStatsSocket)
            outputWriter.write(se.toString());

        for (Bot.statsEntry se : Bot.defendStatsSocket)
            outputWriter.write(se.toString());

        outputWriter.close();


    }

    /**
     * Command line program
     *
     * @param args host
     *             port
     *             RMI Attackers
     *             RMI Defender
     *             Socket Attackers
     *             Socket Defenders
     *             sleepTime(seconds)
     *             AttackerPrimary
     *             AttackerSecondary
     *             AttackerItems
     *             DefenderPrimary
     *             DefenderSecondary
     *             DefenderItems
     * @throws InterruptedException when socket errors occur
     * @throws IOException          when rmi errors occur
     */
    public static void main(String args[]) throws InterruptedException, IOException {
        System.err.println("Creating new bot generator");

        new BotGenerator(args[0], Integer.parseInt(args[1]),
                Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                Integer.parseInt(args[4]), Integer.parseInt(args[5]),
                Long.parseLong(args[6]),
                Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]),
                Integer.parseInt(args[10]), Integer.parseInt(args[11]), Integer.parseInt(args[12]));
    }
}
