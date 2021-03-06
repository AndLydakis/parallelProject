import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Client that uses the RMI interface to interact with the game state<br>
 * possible requests to the server:<br>
 * GETTARGETS request available blocks<br>
 * GETEND get the status of the game state<br>
 * ATTACK/REPAIR attack or repair a block<br>
 * BOMB/SHIELD bomb or shield a block<br>
 * BUYBOMB/BUYSHIELD buy a shield or a bomb<br>
 * LVLATK/LVLREP/LVLSPD request to level up the player skills
 * BOOST request boost
 */
public class RmiClient {
    private RemoteState state;
    private String username;
    private RemotePlayer player;
    private int role;
    private int lastAction;
    private String lastTarget;

    private List<Integer> attackerOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private List<Integer> defenderOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    /**
     * Constructor
     *
     * @param service the rmi service on which the state is running
     * @throws RemoteException       if rmi fails
     * @throws NotBoundException     if rmi fails
     * @throws MalformedURLException if rmi fails
     */
    private RmiClient(String service) throws RemoteException, NotBoundException, MalformedURLException {
        try {
            state = (RemoteState) java.rmi.Naming.lookup(service);
        } catch (Exception e) {
            System.err.println("Could not connect to server, we apologize for the inconvenience");
            System.exit(0);
        }
    }

    /**
     * Print an initial menu when the client first starts
     *
     * @throws IOException if data entry fails
     */
    private void initialMenu() throws IOException {
        int s;
        Scanner reader = new Scanner(System.in);
        do {
            System.out.print("\033[H\033[2J");
            System.err.println("Welcome to the game");
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
                    }
                } while (ch != 1 && ch != 2 && ch != 3);

                if ((ch == 1) || (ch == 2)) {
                    String user;
                    boolean reg;
                    do {
                        reader.nextLine();
                        System.out.print("\033[H\033[2J");
                        System.err.println("Enter your username");
                        System.err.println("Type 'q' to quit");
                        user = reader.nextLine();
                        if (user.equals("q")) {
                            return;
                        }
                        reg = register(user, ch);
                        if (!reg) user = "";
                    } while (user.equals(""));
                    break;
                }
                break;
            case 2:
                reader.nextLine();
                System.out.print("\033[H\033[2J");
                System.err.println("Welcome back, enter your username");
                String li = reader.nextLine();
                login(li);
                break;
            case 3:
                System.exit(0);
                break;
        }
    }

    /**
     * Print a menu with the attacker options
     * and get the user choices
     *
     * @throws RemoteException if rmi fails
     */
    private void AttackerMenu() throws RemoteException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
                System.err.println("------------------");
                System.err.println(player.print());
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
            }
        } while (!attackerOptions.contains(choice));
        processAttackerOptions(choice);
    }

    /**
     * Process the selection made in {@link SocketClient#AttackerMenu()}
     * * and sends the corresponding request to the server
     *
     * @param ch the choice to process
     * @throws RemoteException if rmi fails
     */
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
                int suc = state.requestPrimary(username, role, bl);
                if (suc > 0) {
                    System.err.println("Attacked Block for " + suc + " damage");
                } else if (suc == 0) {
                    System.err.println("Block already at 0 hitpoints");
                } else {
                    System.err.println("Could not reach block");
                }
                lastAction = 2;
                lastTarget = bl;
                break;
            }
            case 3: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
                int suc = state.requestSecondary(username, role, bl);
                if (suc > 0) {
                    System.err.println("Bomb successful for " + suc + " damage");
                } else if (suc == 0) {
                    System.err.println("Block is destroyed");
                } else {
                    System.err.println("Could not reach block");
                }
                lastAction = 3;
                lastTarget = bl;
                break;
            }
            case 4: {
//                int suc = state.buy(username, role);
                int suc = player.buyItem();
                if (suc > 0) {
                    System.err.println("Bomb Purchased: " + suc + " available bombs");
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " credits to buy a bomb");
                } else {
                    System.err.println("Purchase failed");
                }
                lastAction = 4;
                break;
            }
            case 5: {
//                int suc = state.levelPrimary(username, role);
                int suc = player.upgradePrimary();
                if (suc > 0) {
                    System.err.println("Attack Rating Increased to " + suc);
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " to increase attack rating");
                } else {
                    System.err.println("Level up failed");
                }
                lastAction = 5;
                break;
            }
            case 6: {
//                int suc = state.levelSecondary(username, role);
                int suc = player.upgradeSecondary();
                if (suc > 0) {
                    System.err.println("Speed Increased to " + suc);
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " to increase speed");
                } else {
                    System.err.println("Level up failed");
                }
                lastAction = 6;
                break;
            }
            case 7: {
//                int suc = state.requestBoost(username, role);
                int suc = player.boost();
                if (suc > 0) {
                    System.err.println("Speed Temporarily Increased");
                } else if (suc < 0) {
                    System.err.println("Cannot Boost yet");
                } else {
                    System.err.println("Could not reach player");
                }
                lastAction = 7;
                break;
            }
            case 9: {
                logout();
                player = null;
                return;
            }
            case 10: {
                logout();
                System.exit(0);
            }
            case 8: {
                repeat();
                break;
            }
        }
    }

    /**
     * Print a menu with the defenders options
     * and get the user choices
     *
     * @throws RemoteException if rmi fails
     */
    private void DefenderMenu() throws RemoteException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
                System.err.println("------------------");
                System.err.println(state.printPlayer(username, role));
                System.err.println("------------------");
                System.err.println("1. Get List Of Targets");
                System.err.println("2. Repair A Block");
                System.err.println("3. Shield A Block");
                System.err.println("4. Buy A Shield");
                System.err.println("5. Level Up Repair Rating");
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
            }
        } while (!defenderOptions.contains(choice));
        processDefenderOptions(choice);
    }

    /**
     * Process the selection made in {@link SocketClient#DefenderMenu()}
     * * and sends the corresponding request to the server
     *
     * @param ch the choice to process
     * @throws RemoteException if rmi fails
     */
    private void processDefenderOptions(int ch) throws RemoteException {
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
                int suc = state.requestPrimary(username, role, bl);
                if (suc > 0) {
                    System.err.println("Repaired Block for " + suc + " hitpoints");
                } else if (suc == 0) {
                    System.err.println("Block already at full hitpoints or no shields available");
                } else {
                    System.err.println("Could not reach block to repair");
                }
                lastAction = 2;
                lastTarget = bl;
                break;
            }
            case 3: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
                int suc = state.requestSecondary(username, role, bl);
                if (suc > 0) {
                    System.err.println("Block was shielded with " + suc + " shield points");
                } else if (suc == 0) {
                    System.err.println("Block is already shielded");
                } else {
                    System.err.println("Could not reach block to attack");
                }
                lastAction = 3;
                lastTarget = bl;
                break;
            }
            case 4: {
//                int suc = state.buy(username, role);
                int suc = player.buyItem();
                if (suc > 0) {
                    System.err.println("Shield Purchased: " + suc + " available shields");
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " credits to buy a shield");
                } else {
                    System.err.println("Purchase failed");
                }
                lastAction = 4;
                break;
            }
            case 5: {
//                int suc = state.levelPrimary(username, role);
                int suc = player.upgradePrimary();
                if (suc > 0) {
                    System.err.println("Repair Rating Increased to " + suc);
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " to increase repair rating");
                } else {
                    System.err.println("Level up failed");
                }
                lastAction = 5;
                break;
            }
            case 6: {
//                int suc = state.levelSecondary(username, role);
                int suc = player.upgradeSecondary();
                if (suc > 0) {
                    System.err.println("Speed Increased to " + suc);
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " to increase speed");
                } else {
                    System.err.println("Level up failed");
                }
                lastAction = 6;
                break;
            }
            case 7: {
//                int suc = state.requestBoost(username, role);
                int suc = player.boost();
                if (suc > 0) {
                    System.err.println("Speed Temporarily Increased");
                } else if (suc < 0) {
                    System.err.println("Cannot Boost yet");
                } else {
                    System.err.println("Could not reach player");
                }
                lastAction = 7;
                break;
            }
            case 9: {
                logout();
                player = null;
                return;
            }
            case 10: {
                logout();
                System.exit(0);
            }
            case 8: {
                repeat();
                break;
            }
        }
    }

    /**
     * Select the correct menu to display
     *
     * @throws IOException if data entry fails
     */
    private void actionMenu() throws IOException {
        if (role == 1) {
            AttackerMenu();
        } else {
            DefenderMenu();
        }
    }

    private boolean register(String user, int ch) throws RemoteException {
        boolean reg = state.register(user, ch);
        if (reg) {
            player = state.login(user);
            System.err.println("Registered player " + player.unameToString());
            username = user;
            role = ch;
        } else {
            System.err.println("Username already taken");
        }
        return reg;
    }

    /**
     * Request to login with a given username
     *
     * @param li the username of the player
     * @throws RemoteException if rmi fails
     */
    private void login(String li) throws RemoteException {
        player = state.login(li);
        if (player == null) {
            System.err.println("Name not registered, or user is already logged on");
        } else {
            username = player.unameToString();
            role = player.getRole();
            System.err.println("Successfully logged in");
        }
    }


    /**
     * Request to logout
     *
     * @throws RemoteException if rmi fails
     */
    private void logout() throws RemoteException {
        if (state.logout(username)) {
            System.err.println("Successfully logged out");
        } else {
            System.err.println("Could not logout, please retry");
        }
        player = null;
    }

    /**
     * Request to repeat the last action performed
     *
     * @throws RemoteException if rmi fails
     */
    private void repeat() throws RemoteException {
        System.err.println(lastAction);
        System.err.println(lastTarget);
        if (lastAction == 1) {
            System.err.println(state.getTargets());
        } else if (lastAction == 2) {
            int suc = state.requestPrimary(username, role, lastTarget);
            if (suc > 0) {
                if (role == 1) {
                    System.err.println("Attack Successful for " + suc + " hitpoints");
                } else {
                    System.err.println("Repair Successful for " + suc + " hitpoints");
                }
            } else if (suc == 0) {
                if (role == 1) {
                    System.err.println("Could not attack block");
                } else {
                    System.err.println("Could not repair block");
                }
            } else {
                System.err.println("Block unreachable");
            }
        } else if (lastAction == 3) {
            int suc = state.requestSecondary(username, role, lastTarget);
            if (suc > 0) {
                if (role == 1) {
                    System.err.println("Bomb Successful for " + suc + " points");
                } else {
                    System.err.println("Shield Successful for " + suc + " points");
                }
            } else if (suc == 0) {
                if (role == 1) {
                    System.err.println("Could not bomb block");
                } else {
                    System.err.println("Could not shield block");
                }
            } else {
                System.err.println("Block unreachable");
            }

        } else if (lastAction == 4) {
            int suc = state.buy(username, role);
            if (role == 1) {
                if (suc > 0) {
                    System.err.println("Bought bomb, " + suc + " in inventory");
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " credits to buy a bomb");
                } else {
                    System.err.println("Purchase request could not go through");
                }
            } else {
                if (suc > 0) {
                    System.err.println("Bought shield, " + suc + " in inventory");
                } else if (suc < 0) {
                    System.err.println("Need " + (-suc) + " credits to buy a shield");
                } else {
                    System.err.println("Purchase request could not go through");
                }
            }

        } else if (lastAction == 5) {
            int suc = state.levelPrimary(username, role);
            if (suc > 0) {
                if (role == 1) {
                    System.err.println("Attack rating increased to " + suc);
                } else {
                    System.err.println("Repair rating increased to " + suc);
                }
            } else if (suc < 0) {
                if (role == 1) {
                    System.err.println("Need " + (-suc) + " credits to upgrade attack rating");
                } else {
                    System.err.println("Need " + (-suc) + " credits to upgrade repair rating");
                }
            } else {
                System.err.println("Level up request failed");
            }
        } else if (lastAction == 6) {
            int suc = state.levelSecondary(username, role);
            if (suc > 0) {
                System.err.println("Speed increased to " + suc);
            } else if (suc < 0) {
                System.err.println("Need " + (-suc) + " credits to upgrade speed");
            } else {
                System.err.println("Level up request failed");
            }
        } else if (lastAction == 7) {
            int suc = state.requestBoost(username, role);
            if (suc > 0) {
                System.err.println("Speed temporarily increased");
            } else if (suc < 0) {
                System.err.println("Could not boost yet");
            } else {
                System.err.println("Boost request failed");
            }
        }
    }

    /**
     * Request to get the game's state status
     *
     * @throws RemoteException if rmi fails
     */
    private void testEnd() throws RemoteException {
        int status = state.getState();
        if (status == 1) {
            System.err.println("Attackers won, thanks for playing !");
            System.err.println("----- Top Players -----");
            System.err.println(state.printLeaderBoards());
            System.exit(0);
        } else if (status == -1) {
            System.err.println("Defenders won, thanks for playing !");
            System.err.println("----- Top Players -----");
            System.err.println(state.printLeaderBoards());
            System.exit(0);
        } else if (status == 666) {
            System.err.println("Could not get game state");
        }
    }

    public static void main(String args[]) throws IOException, NotBoundException {
        String service = "rmi://" + args[0] + "/" + GameServer.SERVER_NAME;

        RmiClient client = new RmiClient(service);
        while (true) {
            if (client.player == null) {
                client.initialMenu();
            } else {
                client.actionMenu();
                client.testEnd();
            }
        }
//        System.err.println("Thanks for playing, exiting ");
//        return;
    }
}
