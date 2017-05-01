import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lydakis-local on 4/30/17.
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
        String saltStr = salt.toString();
        return saltStr;

    }

    private BotGenerator(int num, String host, int port,
                         double RMIAratio, double RMIDratio,
                         double SocketAratio, double SocketDratio,
                         long sleep,
                         int attackerPrimary, int attackerSecondary, int attackerItems,
                         int defenderPrimary, int defenderSecondary, int defenderItems) throws InterruptedException, IOException {
        String defString = defenderPrimary + "-" + defenderSecondary + "-" + defenderItems;
        String atkString = attackerPrimary + "-" + attackerSecondary + "-" + attackerItems;
        String service = "rmi://" + host + ":" + port + "/" + GameServer.SERVER_NAME;
        RemoteState state = null;
        try {
            state = (RemoteState) java.rmi.Naming.lookup(service);
        } catch (Exception e) {
            System.err.println("Could not connect to server, we apologize for the inconvenience");
            System.exit(0);
        }
        System.err.println("Number of bots: " + num);
        System.err.println("Host: " + host);
        System.err.println("Port: " + port);
        System.err.println("RMIAttacker ratio: " + RMIAratio);
        System.err.println("RMIDefender ratio: " + RMIDratio);
        System.err.println("SocketAttacker ratio: " + SocketAratio);
        System.err.println("SocketDefender ratio: " + SocketDratio);
        System.err.println("Sleep(seconds): " + sleep);
        Random random = new Random();
        ArrayList<Bot> bots = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String username;
            String regString;
//            int role = random.nextDouble() > ADratio ? 0 : 1;

            double roll = random.nextDouble();
            System.err.println(roll);
            System.err.println(RMIAratio);
            System.err.println(RMIAratio + RMIDratio);
            System.err.println(RMIAratio + RMIDratio + SocketAratio);
            System.err.println(RMIAratio + RMIDratio + SocketAratio + SocketDratio);
            if (roll < RMIAratio) {
                username = "RMIAttacker:" + getSaltString();
                regString = username + "-" + 1 + "-" + atkString;
                bots.add(new RmiBot(state, username, 1, (long) (sleep * 1e9), regString));
            } else if (roll < (RMIDratio + RMIAratio)) {
                username = "RMIDefender:" + getSaltString();
                regString = username + "-" + 0 + "-" + defString;
                bots.add(new RmiBot(state, username, 1, (long) (sleep * 1e9), regString));
            } else if (roll < (RMIDratio + RMIAratio + SocketAratio)) {
                username = "SocketAttacker:" + getSaltString();
                regString = username + "-" + 1 + "-" + atkString;
                bots.add(new SocketBot(username, 1, host, port, (long) (sleep * 1e9), regString));
            } else {
                username = "SocketDefender:" + getSaltString();
                regString = username + "-" + 0 + "-" + defString;
                bots.add(new SocketBot(username, 0, host, port, (long) (sleep * 1e9), regString));
            }
            /*
            if (random.nextDouble() > ratio) {
                username = "Socket" + Bot.roles[role] + ":" + getSaltString();
                if (role == 1) {
                    regString = username + "-" + role + "-" + atkString;
                } else {
                    regString = username + "-" + role + "-" + defString;
                }
                bots.add(new SocketBot(username, role, host, port, (long) (sleep * 1e9), regString));
            } else {
                username = "Rmi" + Bot.roles[role] + ":" + getSaltString();
                if (role == 1) {
                    regString = username + "-" + role + "-" + atkString;
                } else {
                    regString = username + "-" + role + "-" + defString;
                }
                bots.add(new RmiBot(state, username, role, (long) (sleep * 1e9), regString));
            }
            */
        }

        for (Bot b : bots) {
            b.start();
        }
        for (Bot b : bots) {
            b.join();
        }
        System.err.println("Saving stats");
        BufferedWriter outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/RMISTATS_" + num + "_" +
                        RMIAratio + "_" + RMIDratio + "_" + SocketAratio + "_" + SocketDratio +
                        ".csv"));
        for (Bot.statsEntry se : Bot.attackStats) {
            outputWriter.write(se.toString());
        }
        for (Bot.statsEntry se : Bot.defendStats) {
            outputWriter.write(se.toString());
        }
        outputWriter.close();
        outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/SOCKETSTATS_" + num + "_" +
                        RMIAratio + "_" + RMIDratio + "_" + SocketAratio + "_" + SocketDratio +
                        ".csv"));
        for (Bot.statsEntry se : Bot.attackStatsSocket) {
            outputWriter.write(se.toString());
        }
        for (Bot.statsEntry se : Bot.defendStatsSocket) {
            outputWriter.write(se.toString());
        }
        outputWriter.close();
    }

    private BotGenerator(int num, String host, int port, double ratio, double ADratio, long sleep) throws InterruptedException, IOException {
        String service = "rmi://" + host + ":" + port + "/" + GameServer.SERVER_NAME;
        RemoteState state = null;
        try {
            state = (RemoteState) java.rmi.Naming.lookup(service);
        } catch (Exception e) {
            System.err.println("Could not connect to server, we apologize for the inconvenience");
            System.exit(0);
        }
        System.err.println("Number of bots: " + num);
        System.err.println("Host: " + host);
        System.err.println("Port: " + port);
        System.err.println("RMI/Socket ratio: " + ratio);
        System.err.println("Attacker/Defender ratio: " + ADratio);
        System.err.println("Sleep(seconds): " + sleep);
        Random random = new Random();
        ArrayList<Bot> bots = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String username;
            int role = random.nextDouble() > ADratio ? 0 : 1;
            if (random.nextDouble() > ratio) {
                username = "Socket" + Bot.roles[role] + ":" + getSaltString();
                bots.add(new SocketBot(username, role, host, port, (long) (sleep * 1e9), ""));
            } else {
                username = "Rmi" + Bot.roles[role] + ":" + getSaltString();
                bots.add(new RmiBot(state, username, role, (long) (sleep * 1e9), ""));
            }
        }
        for (Bot b : bots) {
            b.start();
        }
        for (Bot b : bots) {
            b.join();
        }
        System.err.println("Saving stats");
        BufferedWriter outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") +
                        "/RMISTATS_" + num + "_" + ratio + "_" + ADratio + ".csv"));
        for (Bot.statsEntry se : Bot.attackStats) {
            outputWriter.write(se.toString());
        }
        for (Bot.statsEntry se : Bot.defendStats) {
            outputWriter.write(se.toString());
        }
        outputWriter.close();
        outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/SOCKETSTATS_" + num + "_" + ratio + "_" + ADratio + ".csv"));
        for (Bot.statsEntry se : Bot.attackStatsSocket) {
            outputWriter.write(se.toString());
        }
        for (Bot.statsEntry se : Bot.defendStatsSocket) {
            outputWriter.write(se.toString());
        }
        outputWriter.close();
    }

    /**
     * Command line program
     *
     * @param args numberOfBots host port RMI/Socket-Ratio Attacker/DefenderRatio sleepTime(seconds)
     *             AttackerPrimary AttackerSecondary AttackerItems DefenderPrimary DefenderSecondary DefenderItems
     * @throws InterruptedException when socket errors occur
     * @throws IOException          when rmi errors occur
     */
    public static void main(String args[]) throws InterruptedException, IOException {
        System.err.println("Creating new bot generator");
        if (args.length == 6) {
            new BotGenerator(Integer.parseInt(args[0]), args[1],
                    Integer.parseInt(args[2]), Double.parseDouble(args[3]),
                    Double.parseDouble(args[4]), Long.parseLong(args[5]));
        } else {
            new BotGenerator(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]),
                    Double.parseDouble(args[3]), Double.parseDouble(args[4]),
                    Double.parseDouble(args[5]), Double.parseDouble(args[6]),
                    Long.parseLong(args[7]),
                    Integer.parseInt(args[8]), Integer.parseInt(args[9]), Integer.parseInt(args[10]),
                    Integer.parseInt(args[11]), Integer.parseInt(args[12]), Integer.parseInt(args[13]));
        }
    }
}
