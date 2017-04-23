import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class TestClient {
    private final RemoteState state;
    private String username;
    private RemotePlayer player;

    private TestClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        state = (RemoteState) java.rmi.Naming.lookup(service);
    }

    private void selectTarget() {
        while (true) {

        }
    }

    private void initialMenu() throws IOException {
        int s = 0;
        Scanner reader = new Scanner(System.in);
        do {
            System.out.print("\033[H\033[2J");
            System.err.println("Welcome to the bone-zone");
            System.err.println("Would you like to:");
            System.err.println("1. Sign up");
            System.err.println("2. Sign in");
            System.err.println("3. Quit");
            s = reader.nextInt();
            System.err.println(s);
        } while (s != 1 && s != 2 && s != 3);
        int ch;
        switch (s) {
            case 1:
                do {
                    System.out.print("\033[H\033[2J");
                    System.err.println("Select your role :");
                    System.err.println("1. Attacker");
                    System.err.println("2. Defender");
                    System.err.println("3. Back to menu");
                    ch = reader.nextInt();
                } while (ch != 1 && ch != 2 && ch != 3);

                if ((ch == 1) || (ch == 2)) {
                    String user = "";
                    boolean reg;
                    do {
                        reader.nextLine();
                        System.out.print("\033[H\033[2J");
                        System.err.println("Enter your username (penis jokes will not be tolerated)");
                        System.err.println("Type 'q' to quit");
                        user = reader.nextLine();
                        if (user.equals("q")) {
                            return;
                        }
                        if (user.equals("")) continue;
                        reg = state.register(user, ch);
                        if (reg) {
                            player = state.login(user);
                            System.err.println("Registered player " + player.unameToString());
                        } else {
                            user = "";
                            System.err.println("Username already taken");
                        }
                    } while (user.equals(""));
                    break;
                }
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    private void printMenu() throws RemoteException {
        System.out.print("\033[H\033[2J");
        if (player instanceof Attacker) {
            System.err.printf("******** Attacker %s ********\n" +
                    "Credits: %d, TotalScore :%d\n" +
                    "1.Attack\n" +
                    "2.Bomb\n" +
                    "3.Level Up\n" +
                    "4.Buy Boost\n" +
                    "5.Get Map\n" +
                    "6.Show Leaderboards" +
                    "*****************", username, player.getCredits(), player.getScore());
        } else {
            System.err.printf("******** Defender %s ********\n" +
                    "Credits: %d, TotalScore :%d\n" +
                    "1.Repair\n" +
                    "2.Shield\n" +
                    "3.Level Up\n" +
                    "4.Buy Boost\n" +
                    "5.Get Map\n" +
                    "6.Show Leaderboards" +
                    "*****************", username, player.getCredits(), player.getScore());
        }
    }

    private boolean Register(String un, int role) throws RemoteException {
        if (state.register(un, role)) {
            System.err.println("Successfully registered");
            username = un;
            return true;
        } else {
            System.err.println("Failed to register, username taken");
            return false;
        }
    }

    private void login() throws RemoteException {
        player = (Player) state.login(username);
        if (player == null) {
            System.err.println("Could not login, please retry");
        } else {
            System.err.println("Successfully logged in");
        }
    }

    private void logout() throws RemoteException {
        if (state.logout(username)) {
            System.err.println("Successfully logged out");
        } else {
            System.err.println("Could not logout, please retry");
        }
    }

    public static void main(String args[]) throws IOException, NotBoundException {
        String service = "rmi://" + args[0] + "/" + GameServer.SERVER_NAME;

        TestClient client = new TestClient(service);
        while (true) {
            if (client.player == null) {
                client.initialMenu();
            } else {
                System.err.println("Exiting ");
                return;
            }

        }
    }
}
