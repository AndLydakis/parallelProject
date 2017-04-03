import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class TestClient {
    private final State state;
    private String username;
    private Player player;

    private TestClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        state = (State) java.rmi.Naming.lookup(service);
    }

    private void selectTarget() {
        while (true) {

        }
    }

    private void printMenu() throws RemoteException {
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
        player = state.login(username);
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
}
