import java.io.*;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

/**
 * Client that uses a socket interface to interact with the game state<br>
 * possible requests to the server:<br>
 * GETTARGETS request available blocks<br>
 * GETEND get the status of the game state<br>
 * ATTACK/REPAIR attack or repair a block<br>
 * BOMB/SHIELD bomb or shield a block<br>
 * BUYBOMB/BUYSHIELD buy a shield or a bomb<br>
 * LVLATK/LVLREP/LVLSPD request to level up the player skills<br>
 * BOOST request boost
 */
public class SocketClient {
    private int role;
    private int lastAction;
    private String lastTarget;
    private Player player;
    private String uName;
    private String host;
    private int port;
    private String playerToString;
    private String targets;
    private Socket socket;
    private PrintWriter out;
    BufferedReader in = null;

    private List<Integer> attackerOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private List<Integer> defenderOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    /**
     * Constructor
     *
     * @param host hostname
     * @param port port
     * @throws IOException if socket communication fails
     */
    private SocketClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        while (true) {
            sendRequest("GETEND");
            if (uName == null) {
                initialMenu();
            } else {
                actionMenu();
                testEnd();
            }
        }
    }

    /**
     * Print an initial menu when the client first starts
     *
     * @throws IOException if socket communication fails
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
                        reg = Register(user, ch);
                        System.err.println("*" + reg);
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
     * @throws IOException if socket communication fails
     */
    private void AttackerMenu() throws IOException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
                sendRequest("GETPLAYER-" + uName + "-1");
                System.err.println("------------------");
                System.err.println(playerToString);
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
        System.err.println("Choice : " + choice);
        processAttackerOptions(choice);
    }

    /**
     * Process the selection made in {@link SocketClient#AttackerMenu()}
     * * and sends the corresponding request to the server
     *
     * @param ch the choice to process
     * @throws IOException if socket communication fails
     */
    private void processAttackerOptions(int ch) throws IOException {
        Scanner reader = new Scanner(System.in);
        String bl;
        switch (ch) {
            case 1: {
                sendRequest("GETTARGETS");
                System.err.println(targets);
                lastAction = 1;
                lastTarget = null;
                break;
            }
            case 2: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestPrimary(username, role, bl);
                int suc = sendRequest("ATTACK-" + uName + "-" + bl);
                lastAction = 2;
                lastTarget = bl;
                break;
            }
            case 3: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestSecondary(username, role, bl);
                sendRequest("BOMB-" + uName + "-" + bl);
                lastAction = 3;
                lastTarget = bl;
                break;
            }
            case 4: {
//                int suc = state.buy(username, role);
                sendRequest("BUYBOMB-" + uName);
                lastAction = 4;
                break;
            }
            case 5: {
//                int suc = state.levelPrimary(username, role);
                sendRequest("LVLATK-" + uName);
                lastAction = 5;
                break;
            }
            case 6: {
                sendRequest("LVLSPD-" + uName + "-" + role);
                lastAction = 6;
                break;
            }
            case 7: {
//                int suc = state.requestBoost(username, role);
                sendRequest("BOOST-" + uName + "-" + role);
                lastAction = 7;
                break;
            }
            case 9: {
                player = null;
                uName = null;
                logout();
                return;
            }
            case 10: {
                logout();
                System.exit(0);
                break;
            }
            case 8: {
                System.err.println("repeat 1");
                repeat();
                break;
            }
        }
    }

    /**
     * Print a menu with the defender options
     * and get the user choices
     *
     * @throws IOException if socket communication fails
     */
    private void DefenderMenu() throws IOException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
                sendRequest("GETPLAYER-" + uName + "-0");
                System.err.println("------------------");
                System.err.println(playerToString);
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
     * and sends the corresponding request to the server
     *
     * @param ch the choice to process
     * @throws IOException if socket communication fails
     */
    private void processDefenderOptions(int ch) throws IOException {
        Scanner reader = new Scanner(System.in);
        String bl;
        switch (ch) {
            case 1: {
                sendRequest("GETTARGETS");
                System.err.println(targets);
                lastAction = 1;
                lastTarget = null;
                break;
            }
            case 2: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestPrimary(username, role, bl);
                sendRequest("REPAIR-" + uName + "-" + bl);
                lastAction = 2;
                lastTarget = bl;
                break;
            }
            case 3: {
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestSecondary(username, role, bl);
                sendRequest("SHIELD-" + uName + "-" + bl);
                lastAction = 3;
                lastTarget = bl;
                break;
            }
            case 4: {
//                int suc = state.buy(username, role);
                sendRequest("BUYSHIELD-" + uName);
                lastAction = 4;
                break;
            }
            case 5: {
                sendRequest("LVLREP-" + uName);
                lastAction = 5;
                break;
            }
            case 6: {
//                int suc = state.levelSecondary(username, role);
                sendRequest("LVLSPD-" + uName + "-" + role);
                lastAction = 6;
                break;
            }
            case 7: {
//                int suc = state.requestBoost(username, role);
                sendRequest("BOOST-" + uName + "-" + role);
                lastAction = 7;
                break;
            }
            case 9: {
                player = null;
                uName = null;
                logout();
                return;
            }
            case 10: {
                logout();
                System.exit(0);
            }
            case 8: {
                System.err.println("repeat 2");
                repeat();
                break;
            }
        }
    }

    /**
     * Select the correct menu to display
     *
     * @throws IOException if socket communication fails
     */
    private void actionMenu() throws IOException {
        if (role == 1) {
            AttackerMenu();
        } else {
            DefenderMenu();
        }
    }

    /**
     * Sends a request to the server, receives a reply and process it
     *
     * @param req a String containing the client request
     * @return a String containing the server response
     * @throws IOException if socket communication fails
     */
    private int sendRequest(String req) throws IOException {
        try {

            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                try {
                    StringBuilder resp = new StringBuilder();
                    String line = "";
                    out.print(req + "\r\n");
                    out.flush();
                    while ((line = in.readLine()) != null && line.length() != 0) {
                        resp.append(line + ".");
                    }
//                    System.err.println("Response received :" + resp);
//                resp = processReply(in.readLine());
                    int ret = processReply(resp.toString());
                    return ret;
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Could not connect to server, exiting");
            System.exit(0);
        } finally {
            out.close();
            in.close();
            socket.close();
        }
        return -1;
    }

    /**
     * processes the reply from the server
     *
     * @param reply the reply form the server
     *              possible responses:
     *              REGISTER-(1/0)-USERNAME-ROLE.
     *              LOGIN-(1/0)-USERNAME-ROLE.
     *              LOGOUT-(1/0).
     *              ATTACK-(POINTS_GAINED/0/FAIL).
     *              REPAIR-(POINTS_GAINED/0/FAIL).
     *              SHIELD-(POINTS_GAINED/0/FAIL).
     *              BOMB-(POINTS_GAINED/0/FAIL).
     *              BUYBOMB/BUYSHIELD-(NUMBER_OF_ITEMS/CREDITS_NEEDED_TO_BUY/FAIL).
     *              LVLATK/LVLREP/LVLSPD-(NEW_STAT_VALUE/CREDITS_NEEDED_TO_BUY/FAIL).
     *              BOOST-(1/0).
     *              GETEND-(1/-1/0/666).
     *              TARGETS-TARGET1.TARGET2.TARGET3...TARGETN.
     * @return an integer depending on the success or failure of each request
     */
    private int processReply(String reply) {
//        System.err.println("Processing: " + reply);
        String tokens[] = reply.split("-");
        int res = -1;
        switch (tokens[0]) {
            case "REGISTER": {
                if (Integer.parseInt(tokens[1]) == 1) {
                    uName = tokens[2];
                    role = Integer.parseInt(
                            tokens[3].replace(".", ""));
                    return 1;
                }
                return -1;
            }
            case "TARGETS": {
                targets = tokens[1].replace(".", "\n");
                System.err.println(targets);
                break;
            }
            case "LOGIN": {
                res = Integer.parseInt(tokens[1]);
                if (res == 1) {
                    uName = tokens[2];
                    role = Integer.parseInt(tokens[3].replace(".", ""));
                } else {
                    System.err.println("Could not login");
                }
                break;
            }
            case "LOGOUT": {
                res = Integer.parseInt(tokens[1]);
                if (res == 1) {
                    System.err.println("Successfully logged out");
                    uName = null;
                } else {
                    System.err.println("Could not log out");
                }
                break;
            }
            case "ATTACK": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Successfully hit for " + res + " damage");
                } else if (res == 0) {
                    System.err.println("Attack on cooldown");
                } else {
                    System.err.println("Could not reach block");
                }
                break;
            }
            case "REPAIR": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Successfully repaired " + res + " hitpoints");
                } else if (res == 0) {
                    System.err.println("Repair on cooldown");
                } else {
                    System.err.println("Could not reach block");
                }
                break;
            }
            case "BOMB": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Successfully bombed for " + res + " damage");
                } else if (res == 0) {
                    System.err.println("Block already destroyed");
                } else {
                    System.err.println("Could not reach block");
                }
                break;
            }
            case "SHIELD": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Successfully shielded for " + res + " damage");
                } else if (res == 0) {
                    System.err.println("Could not shield");
                } else {
                    System.err.println("Could not reach block");
                }
                break;
            }
            case "BUYBOMB": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Bomb bought, " + res + " bombs in inventory");
                } else if (res == 0) {
                    System.err.println("Need " + (-res) + " credits to buy a bomb");
                } else {
                    System.err.println("Could perform purchase");
                }
                break;
            }
            case "BUYSHIELD": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Shield bought, " + res + " shields in inventory");
                } else if (res == 0) {
                    System.err.println("Need " + (-res) + " credits to buy a shield");
                } else {
                    System.err.println("Could perform purchase");
                }
                break;
            }
            case "LVLATK": {
                System.err.println(reply);
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Attack rating increased to " + res);
                } else if (res == 0) {
                    System.err.println("Need " + (-res) + " credits to level up attack rating");
                } else {
                    System.err.println("Could not perform level up");
                }
                break;
            }
            case "LVLREP": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Repair rating increased to " + res);
                } else if (res == 0) {
                    System.err.println("Need " + (-res) + " credits to level up attack rating");
                } else {
                    System.err.println("Could perform level up");
                }
                break;
            }
            case "LVLSPD": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Speed increased to " + res);
                } else if (res == 0) {
                    System.err.println("Need " + (-res) + " credits to level up speed");
                } else {
                    System.err.println("Could perform level up");
                }
                break;
            }
            case "BOOST": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Speed temporarily increased");
                } else if (res == 0) {
                    System.err.println("Can't boost yet");
                } else {
                    System.err.println("Could reach server");
                }
                break;
            }
            case "GETPLAYER": {
                playerToString = tokens[1].replace(".", "\n");
                break;
            }
            case "GETEND": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res == 0) break;
                try {
                    if (res == 666) {
                        System.err.println("Game crashed, we apologise for the inconvenience");
                    }
                    if (res == 1) {
                        System.err.println("Attackers won, thanks for playing");
                        System.err.println("----- Top Players -----");
                        System.err.println(tokens[2].replace(".", "\n"));
                    }
                    if (res == -1) {
                        System.err.println("Defenders won, thanks for playing");
                        System.err.println("----- Top Players -----");
                        System.err.println(tokens[2].replace(".", "\n"));
                    }
                } catch (Exception e) {
                    System.err.println("Game crashed, we apologise for the inconvenience");
                }
                System.exit(0);
            }
        }
        return res;
    }

    /**
     * Request to apply the player's primary ability
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestPrimary() throws IOException {
        if (role == 1) {
            return sendRequest("ATTACK-" + uName + "-" + lastTarget);
        } else {
            return sendRequest("REPAIR-" + uName + "-" + lastTarget);
        }
    }

    /**
     * Request to apply the player's secondary ability
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestSecondary() throws IOException {
        if (role == 1) {
            return sendRequest("BOMB-" + uName + "-" + lastTarget);
        } else {
            return sendRequest("SHIELD-" + uName + "-" + lastTarget);
        }
    }

    /**
     * Request to buy an item
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestBuy() throws IOException {
        if (role == 1) {
            return sendRequest("BUYBOMB-" + uName);
        } else {
            return sendRequest("BUYSHIELD-" + uName);
        }
    }

    /**
     * Request to upgrade the player's primary ability
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestLVL() throws IOException {
        if (role == 1) {
            return sendRequest("LVLATK-" + uName);
        } else {
            return sendRequest("LVLREP-" + uName);
        }
    }

    /**
     * Request to apply the player's speed
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestLVLSPD() throws IOException {
        return sendRequest("LVLSPD-" + uName + "-" + role);
    }

    /**
     * Request to apply a boost to the player's speed
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int requestBoost() throws IOException {
        return sendRequest("BOOST-" + uName + "-" + role);
    }

    /**
     * Request to get the game's state status
     *
     * @return the success status of the request
     * @throws IOException if socket communication fails
     */
    private int testEnd() throws IOException {
        return sendRequest("END");
    }

    /**
     * Request to register a username
     *
     * @param username the username to register
     * @param role     the role of the new player
     * @return true if the registration was successful, false otherwise
     * @throws IOException if socket communication fails
     */
    private boolean Register(String username, int role) throws IOException {
        return sendRequest("REGISTER-" + username + "-" + role) == 1;

    }

    /**
     * Request to login with a given username
     *
     * @param username the username of the player
     * @throws IOException if socket communication fails
     */
    private void login(String username) throws IOException {
        sendRequest("LOGIN-" + username + "-" + role);
    }

    /**
     * Request to logout
     *
     * @throws IOException if socket communication fails
     */
    private void logout() throws IOException {
        sendRequest("LOGOUT-" + uName + "-" + role);
    }

    /**
     * Request to repeat the last action performed
     *
     * @throws IOException if socket communication fails
     */
    private void repeat() throws IOException {
        if (lastAction == 1) {
            sendRequest("GETTARGETS");
            System.err.println(targets);
        } else if (lastAction == 2) {
//            int suc = state.requestPrimary(username, role, lastTarget);
            requestPrimary();
        } else if (lastAction == 3) {
//            int suc = state.requestSecondary(username, role, lastTarget);
            requestSecondary();
        } else if (lastAction == 4) {
//            int suc = state.buy(username, role);
            requestBuy();
        } else if (lastAction == 5) {
//            int suc = state.levelPrimary(username, role);
            requestLVL();
        } else if (lastAction == 6) {
//            int suc = state.levelSecondary(username, role);
            requestLVLSPD();
        } else if (lastAction == 7) {
//            int suc = state.requestBoost(username, role);
            requestBoost();
        }
    }

    public static void main(String args[]) throws IOException {
        new SocketClient(args[0], Integer.parseInt(args[1]));
    }
}
