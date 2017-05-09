import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A server generates a cube with the given dimensions and
 * enables an administrator to monitor the server status
 */
public class GameServer {

    static final String SERVER_NAME = "server";
    private final LocalState state;
    private Registry registry;

    /**
     * ServerThreads are used to run socket requests from clients
     */
    static class ServerThread implements Runnable {
        String line = null;
        BufferedReader is = null;
        PrintWriter os = null;
        Socket s = null;
        RemoteState state = null;

        /**
         * create a thread with a given socket and game state
         *
         * @param s  socket
         * @param st game state
         */
        ServerThread(Socket s, RemoteState st) {
            this.s = s;
            this.state = st;
        }

        public void run() {

            /*
              Connect to socket
             */

            try {
                is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                os = new PrintWriter(s.getOutputStream());
            } catch (IOException e) {
                System.out.println("IO error in server thread");
                e.printStackTrace();
            }

            /*
             * Read/Parse Command
             */

            try {
                line = is.readLine();
//                System.err.println(line);
                String s = state.parseRequest(line);
//                System.err.println("Response -> " + s);
                os.println(s);
                os.flush();
            } catch (IOException e) {
                System.out.println("IO Error/ Client " + line + " terminated abruptly");
                e.printStackTrace();
            } catch (NullPointerException e) {
//                line = this.getName(); //reused String line for getting thread name
                System.out.println("Client " + line + " Closed");
            } finally {
                try {
//                    System.err.println("Connection Closing");
                    if (is != null) {
                        is.close();
//                        System.out.println(" Socket Input Stream Closed");
                    }
                    if (os != null)
                        os.close();
//                        System.out.println("ppoppp");
                    if (s != null) {
                        s.close();
//                        System.out.println("Socket Closed");
                    }
                } catch (IOException e) {
                    System.out.println("Socket Close Error");
                }
            }
        }
    }

    /**
     * create a server for the given game
     */
    private GameServer(LocalState localState) {
        this.state = localState;
    }


    /**
     * http://stackoverflow.com/a/14541376/1440902
     *
     * @return
     * @throws Exception
     */
    private static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Start the server by binding it to a registry
     * <p>
     * <ul>
     * <p>
     * <li>If {@code port} is positive, the server attempts to locate a registry at this port.</li>
     * </p>
     * <p>
     * <li>If {@code port} is negative, the server attempts to start a registry at this port.</li>
     * </p>
     * <p>
     * <li>If {@code port} is 0, the server attempts to locate a registry at a random port.</li>
     * </p>
     * </ul>
     * </p>
     */
    private synchronized int start(int port) throws RemoteException, UnknownHostException {

        String ip;

        try {
            ip = getIp();
        } catch (Exception e) {
            ip = InetAddress.getLocalHost().getHostAddress();
        }

        System.setProperty("java.rmi.server.hostname", ip);


        if (registry != null)
            throw new IllegalStateException("Server already running");
        Registry reg;
        if (port > 0) {
            System.err.println("Getting registry at port: " + port);
            reg = LocateRegistry.createRegistry(port);
            System.err.println("Got registry");
        } else if (port < 0) {
            port = -port;
            reg = LocateRegistry.createRegistry(port);
        } else {
            Random rand = new Random();
            int tries = 0;
            while (true) {
                port = 50000 + rand.nextInt(10000);
                try {
                    reg = LocateRegistry.createRegistry(port);
                    break;
                } catch (RemoteException re) {
                    if (++tries < 10 && re.getCause() instanceof java.net.BindException)
                        continue;
                    throw re;
                }
            }
        }
        System.err.println("Rebinding registry for " + state.name);
        reg.rebind(state.name, state);
        System.err.println("Bind successful");
        return port;
    }

    /**
     * Stops the server by removing the game from the registry
     */
    private synchronized void stop() {
        if (registry != null) {
            try {
                registry.unbind(state.name);
            } catch (Exception e) {
                System.err.printf("unable to stop: %s%n", e.getMessage());
            } finally {
                registry = null;
            }
        }
    }

    /**
     * prints the status of the server
     *
     * @throws RemoteException if rmi fails
     */
    private void printStatus() throws RemoteException {
        try {
            this.state.printStatus();
            this.state.printTimeLeft();
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }

    /**
     * Command line program to create a new state or load a saved state from a serialized object
     *
     * @param args Commandline arguments:
     *             port
     *             server_name
     *             cube_size
     *             block_hitpoints
     *             time_limit(seconds)
     *             (serialized_state)
     * @throws IOException if socket communication fails
     */
    public static void main(String[] args) throws IOException {
        int port = 0;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        String name = args[1];
        int size = Integer.parseInt(args[2]);
        int bhp = Integer.parseInt(args[3]);
        int tl = Integer.parseInt(args[4]);
        LocalState state = null;
        if (args.length == 5) {
            state = new LocalState(name, size, bhp, tl);
        } else {
            ObjectInputStream objectinputstream = null;
            try {
                FileInputStream streamIn = new FileInputStream(args[5]);
                objectinputstream = new ObjectInputStream(streamIn);
                System.err.println("Loading state from " + args[5]);
                state = (LocalState) objectinputstream.readObject();
                System.err.println("Resetting locks");
                state.reset();
            } catch (Exception e) {
                System.err.println("Could not load state from " + args[5] + ", exiting");
                e.printStackTrace();
                System.exit(0);
            } finally {
                if (objectinputstream != null) {
                    objectinputstream.close();
                }
            }
        }

        GameServer server = new GameServer(state);
        try {
            port = server.start(port);

            ExecutorService clientExecutor = Executors.newFixedThreadPool(16);

            AdminMenu menu = new AdminMenu(state);
            menu.start();

            ScheduledExecutorService cubeExecutor = Executors.newScheduledThreadPool(1);
            try {
                cubeExecutor.scheduleAtFixedRate(() -> {
                    try {
                        server.printStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 10, 10, SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }


            Socket s;
            ServerSocket serverSocket = new ServerSocket(port + 1);
            while (true) {
                try {
                    s = serverSocket.accept();
//                    ServerThread st = new ServerThread(s);
//                    st.start();
                    clientExecutor.submit(new ServerThread(s, state));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Connection error");
                    break;
                }
            }
            clientExecutor.shutdown();
        } catch (RemoteException re) {
            Throwable t = re.getCause();
            if (t instanceof java.net.ConnectException) {
                System.err.println("Unable to connecto to registry: " + re.getMessage());
            } else if (t instanceof java.net.BindException) {
                System.err.println("Cannot start registry: " + re.getMessage());
            } else {
                System.err.println("Cannot start server: " + re.getMessage());
            }
            UnicastRemoteObject.unexportObject(state, false);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
