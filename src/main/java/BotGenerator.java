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

    public BotGenerator(int num, String host, int port, double ratio, long sleep) {
        String service = "rmi://" + host + ":" + port + "/" + GameServer.SERVER_NAME;
        RemoteState state = null;
        try {
            state = (RemoteState) java.rmi.Naming.lookup(service);
        } catch (Exception e) {
            System.err.println("Could not connect to server, we apologize for the inconvenience");
            System.exit(0);
        }
        Random random = new Random();
        ArrayList<Bot> bots = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String username = getSaltString();
            if (random.nextDouble() > ratio) {
                bots.add(new SocketBot(username, random.nextInt(2), host, port, sleep));
            } else {
                bots.add(new RmiBot(state, username, random.nextInt(2), sleep));
            }

        }
    }

    public static void main(String args[]) {
        System.err.println("Creating new bot generator");
        BotGenerator gen = new BotGenerator(Integer.parseInt(args[0]), args[1],
                Integer.parseInt(args[2]), Double.parseDouble(args[3]), Long.parseLong(args[4]));
    }
}
