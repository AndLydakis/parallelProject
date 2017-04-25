import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class TestClient {
    private final RemoteState state;
    private String username;
    private RemotePlayer player;
    private int role;
    private int lastAction;
    private String lastTarget;

    List<Integer> attackerOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    List<Integer> defenderOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);


    private TestClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        state = (RemoteState) java.rmi.Naming.lookup(service);
    }

    private void repeat() throws RemoteException {
        System.err.println(lastAction);
        System.err.println(lastTarget);
        if(lastAction == 1){
            System.err.println(state.getTargets());
        }
        else if (lastAction == 2){
            boolean suc = state.requestPrimary(username,role,lastTarget);
            if (suc) {
                if(role == 1) {
                    System.err.println("Attack Successful");
                }else{
                    System.err.println("Repair Successful");
                }
            } else {
                if(role == 1) {
                    System.err.println("Could not attack block");
                }else{
                    System.err.println("Could not repair block");
                }
            }
        }else if (lastAction == 3){
            boolean suc = state.requestSecondary(username, role, lastTarget);
            if (suc) {
                if(role == 1) {
                    System.err.println("Bomb Successful");
                }else{
                    System.err.println("Shield Successful");
                }
            } else {
                if(role == 1) {
                    System.err.println("Could not bomb block");
                }else{
                    System.err.println("Could not shield block");
                }
            }

        }else if(lastAction == 4){
            boolean suc = state.levelPrimary(username, role);
            if (suc) {
                System.err.println("Level up successful");
            } else {
                System.err.println("Not enough credits to level up");
            }
        }else if(lastAction == 5){
            boolean suc = state.levelSecondary(username, role);
            if (suc) {
                System.err.println("Level up successful");
            } else {
                System.err.println("Not enough credits to level up");
            }
        }else if(lastAction == 7){
            boolean suc = state.requestBoost(username);
            if (suc) {
                System.err.println("Speed temporarily increased");
            } else {
                System.err.println("Could not boost");
            }
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
        } while (s != 1 && s != 2 && s != 3);
        int ch;
        switch (s) {
            case 1:
                do {
                    try {
                        System.out.print("\033[H\033[2J");
                        System.err.println("Select your role :");
                        System.err.println("1. Attacker");
                        System.err.println("2. Defender");
                        System.err.println("3. Back to menu");
                        ch = reader.nextInt();
                    } catch (Exception e) {
                        System.err.println("Invalid Input");
                        reader.nextLine();
                        ch = -1;
                        continue;
                    }
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
                        reg = state.register(user, ch);
                        if (reg) {
                            player = state.login(user);
                            System.err.println("Registered player " + player.unameToString());
                            username = user;
                            role = ch;
                        } else {
                            user = "";
                            System.err.println("Username already taken");
                        }
                    } while (user.equals(""));
                    break;
                }
                break;
            case 2:
                reader.nextLine();
                System.out.print("\033[H\033[2J");
                System.err.println("Welcome back, enter your username");
                String li = reader.nextLine();
                player = state.login(li);
                username = player.unameToString();
                role = player.getRole();
                break;
            case 3:
                System.exit(-1);
                break;
        }
    }

    private void AttackerMenu() throws RemoteException {
        Scanner reader = new Scanner(System.in);
        int choice = -1;
        do {
            try {
                System.err.println("------------------");
                System.err.println(state.printPlayer(username, role));
                System.err.println("------------------");
                System.err.println("1. Get List Of Targets");
                System.err.println("2. Attack A Block");
                System.err.println("3. Bomb A Block");
                System.err.println("4. Buy A Bomb");
                System.err.println("5. Level Up Attack Rating");
                System.err.println("6. Level Up Speed");
                System.err.println("7. Boost");
                System.err.println("8. Repeat Last Action");
                System.err.println("9. Back to Menu");
                System.err.println("10. Exit");
                choice = reader.nextInt();
            } catch (Exception e) {
                System.err.println("Invalid Input");
                reader.nextLine();
                choice = -1;
                continue;
            }
        } while (!attackerOptions.contains(choice));
        processAttackerOptions(choice);

    }

    private void processAttackerOptions(int ch) throws RemoteException {
        Scanner reader = new Scanner(System.in);
        String bl;
        switch (ch) {
            case 1: {
                System.err.println(state.getTargets());
                lastAction = 1;
                lastTarget = null;
                break;
            }
            case 2: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
                boolean suc = state.requestPrimary(username, role, bl);
                if (suc) {
                    System.err.println("Attack Successful");
                } else {
                    System.err.println("Attack Failed");
                }
                lastAction = 2;
                lastTarget = bl;
                break;
            }
            case 3: {
                reader.nextLine();
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
                boolean suc = state.requestSecondary(username, role, bl);
                if (suc) {
                    System.err.println("Bomb Successful");
                } else {
                    System.err.println("Bomb Failed");
                }
                lastAction = 3;
                lastTarget = bl;
                break;
            }
            case 4: {
                boolean suc = state.buy(username, role);
                if (suc) {
                    System.err.println("Bomb Purchased");
                } else {
                    System.err.println("Not enough credits");
                }
                lastAction = 4;
                break;
            }
            case 5: {
                boolean suc = state.levelPrimary(username, role);
                if (suc) {
                    System.err.println("Attack Rating Increased");
                } else {
                    System.err.println("Not enough credits");
                }
                lastAction = 5;
                break;
            }
            case 6: {
                boolean suc = state.levelSecondary(username, role);
                if (suc) {
                    System.err.println("Speed Rating Increased");
                } else {
                    System.err.println("Not enough credits");
                }
                lastAction = 6;
                break;
            }

            case 7: {
                boolean suc = state.requestBoost(username);
                if (suc) {
                    System.err.println("Speed Temporarily Increased");
                } else {
                    System.err.println("Cannot Boost yet");
                }
                lastAction = 7;
                break;
            }
            case 9: {
                player = null;
                return;
            }
            case 10: {
                System.exit(-1);
            }
            case 8: {
                repeat();
            }
        }
    }

    private void DefenderMenu() {

    }

    private void actionMenu() throws IOException {
        if (role == 1) {
            AttackerMenu();
        } else {
            DefenderMenu();
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
                client.actionMenu();
            }
        }
//        System.err.println("Thanks for playing, exiting ");
//        return;
    }
}
