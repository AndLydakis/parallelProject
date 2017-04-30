import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/30/17.
 */
public class SocketBot extends Bot {

    private int role;
    private int numOps;
    private int avgDelay;
    private int port;
    private long sleep;
    private String targets;
    private String host;
    private String username;
    private volatile boolean running;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private PrintWriter out;
    BufferedReader in = null;
    BufferedReader read = new BufferedReader(new InputStreamReader(System.in));

    private void addStats() {
        if (role == 1) {
            synchronized (attackStats) {
                attackStatsSocket.add(new statsEntry(role, numOps, avgDelay));
            }
        } else {
            synchronized (defendStats) {
                defendStatsSocket.add(new statsEntry(role, numOps, avgDelay));
            }
        }
    }

    public SocketBot(String username, int role, String host, int port, long sleep) {
        this.running = true;
        this.host = host;
        this.port = port + 1;
        this.numOps = 0;
        this.username = username;
        this.sleep = sleep;
        if (role == 1) {
            System.err.println("Created new socket attacker bot");
        } else {
            System.err.println("Created new socket defender bot");
        }
    }

    private int requestPrimary() throws IOException {
        String tokens[] = targets.split("\n");
        String target = tokens[0];
        if (target == null) {
            running = false;
            return -1;
        }
        int res;
        if (role == 1) {
            res = sendRequest("ATTACK-" + username + "-" + target);
        } else {
            res = sendRequest("REPAIR-" + username + "-" + target);
        }
        return res;
    }

    private void getTargets() throws IOException {
        sendRequest("GETTARGETS");
        if (targets == null) {
            running = false;
        }
    }

    private int processReply(String reply) {
        System.err.println("Processing: " + reply);
        String tokens[] = reply.split("-");
        System.err.println(tokens);
        int res;
        switch (tokens[0]) {
            case "REGISTER": {
                if (Integer.parseInt(tokens[1]) == 1) {
                    username = tokens[2];
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
                    username = tokens[2];
                    role = Integer.parseInt(tokens[3].replace(".", ""));
                } else {
                    System.err.println("Could not login");
                }
                break;
            }
            case "ATTACK": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res > 0) {
                    System.err.println("Successfully hit for " + res + " damage");
                } else if (res == 0) {
                    System.err.println("Block already destroyed");
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
                    System.err.println("Block already at full hitpoints");
                } else {
                    System.err.println("Could not reach block");
                }
                break;
            }
            case "GETEND": {
                res = Integer.parseInt(reply.substring(
                        reply.indexOf("(") + 1, reply.indexOf(")")));
                if (res == 0) break;
                try {
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
        return 0;
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
                        resp.append(line + ".");
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
            System.exit(0);
        } finally {
            out.close();
            in.close();
            socket.close();
        }
        return -1;
    }

    public void run() {
        long start;
        try {
            if (sendRequest("REGISTER-" + username + "-" + role) != 1)
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (running) {
            try {
                if (targets == null) {
                    continue;
                }
                start = System.nanoTime();
                if (sendRequest("GETEND") != 0) {
                    running = false;
                }
                avgDelay += (System.nanoTime() - start);
                numOps++;
                start = System.nanoTime();
                if (requestPrimary() < 0) {
                    running = false;
                    break;
                }
                avgDelay += (System.nanoTime() - start);
                numOps++;
                Thread.sleep(sleep);

            } catch (Exception e) {
                e.printStackTrace();
                if (numOps != 0) {
                    avgDelay /= numOps;
                    addStats();
                    return;
                }
            }
        }
        avgDelay /= numOps;
        addStats();
    }
}
