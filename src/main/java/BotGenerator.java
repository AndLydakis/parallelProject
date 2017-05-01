import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lydakis-local on 4/30/17.
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

    public BotGenerator(int num, String host, int port, double ratio, double ADratio, long sleep,
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
        System.err.println("RMI/Socket ratio: " + ratio);
        System.err.println("Attacker/Defender ratio: " + ADratio);
        System.err.println("Sleep(seconds): " + sleep);
        Random random = new Random();
        ArrayList<Bot> bots = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String username;
            String regString;
            int role = random.nextDouble() > ADratio ? 0 : 1;

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
        }
        for (Bot b : bots) {
            b.start();
        }
        for (Bot b : bots) {
            b.join();
        }
        System.err.println("Saving stats");
        BufferedWriter outputWriter = new BufferedWriter(
                new FileWriter(System.getProperty("user.home") + "/RMISTATS_" + num + "_" + ratio + "_" + ADratio + ".csv"));
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

    public BotGenerator(int num, String host, int port, double ratio, double ADratio, long sleep) throws InterruptedException, IOException {
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
                new FileWriter(System.getProperty("user.home") + "/RMISTATS_" + num + "_" + ratio + "_" + ADratio + ".csv"));
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

    /*
    Command Line arguments : numberOfBots  host port RMI/Socket-Ratio Attacker/DefenderRatio sleepTime(seconds)
    AttackerPrimary AttackerSecondary AttackerItems DefenderPrimary DefenderSecondary DefenderItems

     */
    public static void main(String args[]) throws InterruptedException, IOException {
        System.err.println("Creating new bot generator");
        if (args.length == 6) {
            new BotGenerator(Integer.parseInt(args[0]), args[1],
                    Integer.parseInt(args[2]), Double.parseDouble(args[3]),
                    Double.parseDouble(args[4]), Long.parseLong(args[5]));
        } else {
            new BotGenerator(Integer.parseInt(args[0]), args[1],
                    Integer.parseInt(args[2]), Double.parseDouble(args[3]),
                    Double.parseDouble(args[4]), Long.parseLong(args[5]),
                    Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]),
                    Integer.parseInt(args[9]), Integer.parseInt(args[10]), Integer.parseInt(args[11]));
        }
    }
}
