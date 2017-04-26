import java.io.*;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by lydakis-local on 4/3/17.
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
    private OutputStream outputStream;
    private InputStream inputStream;
    private PrintWriter out;
    BufferedReader in = null;
    BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

    List<Integer> attackerOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    List<Integer> defenderOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    public void initialMenu() throws IOException {
        int s;
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
                    }
                } while (ch != 1 && ch != 2 && ch != 3);
                if ((ch == 1) || (ch == 2)) {
                    String user;
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
                System.exit(-1);
                break;
        }
    }


    private void AttackerMenu() throws IOException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
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
        processAttackerOptions(choice);
    }

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
                int suc = sendRequest("ATTACK-" + player + "-" + bl);
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
                reader.nextLine();
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestSecondary(username, role, bl);
                int suc = sendRequest("BOMB-" + player + "-" + bl);
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
                int suc = sendRequest("BUYBOMB-" + player);
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
                int suc = sendRequest("LVLATK-" + player);
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
                int suc = sendRequest("LVLSPD-" + player);
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
                int suc = sendRequest("BOOST-ATK-" + player);
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

    private void DefenderMenu() throws IOException {
        Scanner reader = new Scanner(System.in);
        int choice;
        do {
            try {
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
                int suc = sendRequest("REPAIR-" + player + "-" + bl);
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
                reader.nextLine();
                System.err.println("Type in Block Coordinates : (X_Y_Z)");
                bl = reader.nextLine();
//                int suc = state.requestSecondary(username, role, bl);
                int suc = sendRequest("SHIELD-" + player + "-" + bl);
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
                int suc = sendRequest("BUYSHIELD-" + player);
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
                int suc = sendRequest("LVLREP-" + player);
//                int suc = state.levelPrimary(username, role);
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
                int suc = sendRequest("LVLSPD-" + player);
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
                int suc = sendRequest("BOOST-" + player + "-" + role);
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

    private void actionMenu() throws IOException {
        if (role == 1) {
            AttackerMenu();
        } else {
            DefenderMenu();
        }
    }

    private int processReply(String reply) {
        System.err.println("Processing: " + reply);
        String tokens[] = reply.split("-");
        switch (tokens[0]) {
            case "REGISTER": {
                if (Integer.parseInt(tokens[1]) == 1) {
                    uName = tokens[2];
                    role = Integer.parseInt(tokens[3].replaceAll(".",""));
                    return 1;
                }
                return -1;
            }
            case "TARGETS":{
                String[] t = tokens[1].split(".");
                targets = "";
                for(String tar : t){
                    targets += (t+"\n");
                }
                System.err.println(targets);
            }
        }
        return 0;
    }

    private boolean Register(String username, int role) throws IOException {
        return sendRequest("REGISTER-" + username + "-" + role) == 1;

    }


    private void login(String username) throws IOException {
        sendRequest("LOGIN-" + username + "-" + role);
    }


    private void logout() throws IOException {
        sendRequest("LOGOUT-" + player + "-" + role);
    }

    private int requestPrimary() throws IOException {
        if (role == 1) {
            return sendRequest("ATTACK-" + player + "-" + lastTarget);
        } else {
            return sendRequest("REPAIR-" + player + "-" + lastTarget);
        }
    }

    private int requestSecondary() throws IOException {
        if (role == 1) {
            return sendRequest("BOMB-" + player + "-" + lastTarget);
        } else {
            return sendRequest("SHIELD-" + player + "-" + lastTarget);
        }
    }

    private int requestBuy() throws IOException {
        if (role == 1) {
            return sendRequest("BUYBOMB-" + player);
        } else {
            return sendRequest("BUYSHIELD-" + player);
        }
    }

    private int requestLVL() throws IOException {
        if (role == 1) {
            return sendRequest("LVLATK-" + player);
        } else {
            return sendRequest("LVLREP-" + player);
        }
    }

    private int requestLVLSPD() throws IOException {
        return sendRequest("LVLSPD-" + player + "-" + role);
    }

    private int requestBoost() throws IOException {
        return sendRequest("BOOST-" + player + "-" + role);
    }

    private void repeat() throws IOException {
        System.err.println(lastAction);
        System.err.println(lastTarget);
        if (lastAction == 1) {
            sendRequest("GETTARGETS");
            System.err.println(targets);
        } else if (lastAction == 2) {
//            int suc = state.requestPrimary(username, role, lastTarget);
            int suc = requestPrimary();
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
//            int suc = state.requestSecondary(username, role, lastTarget);
            int suc = requestSecondary();
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
//            int suc = state.buy(username, role);
            int suc = requestBuy();
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
//            int suc = state.levelPrimary(username, role);
            int suc = requestLVL();
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
//            int suc = state.levelSecondary(username, role);
            int suc = requestLVLSPD();
            if (suc > 0) {
                System.err.println("Speed increased to " + suc);
            } else if (suc < 0) {
                System.err.println("Need " + (-suc) + " credits to upgrade speed");
            } else {
                System.err.println("Level up request failed");
            }
        } else if (lastAction == 7) {
//            int suc = state.requestBoost(username, role);
            int suc = requestBoost();
            if (suc > 0) {
                System.err.println("Speed temporarily increased");
            } else if (suc < 0) {
                System.err.println("Could not boost yet");
            } else {
                System.err.println("Boost request failed");
            }
        }
    }

    private int testEnd() throws IOException {
        return sendRequest("END");
    }


    private int sendRequest(String req) throws IOException {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outputStream = socket.getOutputStream();

            while (true) {
                try {
                    StringBuilder resp = new StringBuilder();
                    String line = "";
                    out.print(req + "\r\n");
                    out.flush();
                    while ((line = in.readLine()) != null && line.length() != 0) {
                        resp.append(line+".");
                    }
                    System.err.println("Response received :" + resp);
//                resp = processReply(in.readLine());
                    int ret = processReply(resp.toString());
                    return ret;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Could not connect to server, exiting");
            System.exit(-1);
        } finally {
            out.close();
            in.close();
            socket.close();
        }
        return -1;
    }

    private SocketClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        while (true) {
            if (uName == null) {
                initialMenu();
            } else {
                actionMenu();
                testEnd();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        SocketClient sc = new SocketClient(args[0], Integer.parseInt(args[1]));
    }
}
