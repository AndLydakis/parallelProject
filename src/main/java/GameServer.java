import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class GameServer {

    private final LocalState state;
    private Registry registry;

    static class ServerThread implements Runnable {
        String line = null;
        BufferedReader is = null;
        PrintWriter os = null;
        Socket s = null;
        RemoteState state = null;

        public ServerThread(Socket s, RemoteState st) {
            this.s = s;
            this.state = st;
        }

        public void run() {
            /**
             * Connect to socket
             */
            try {
                is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                os = new PrintWriter(s.getOutputStream());
            } catch (IOException e) {
                System.out.println("IO error in server thread");
                e.printStackTrace();
            }
            /**
             * Read/Parse Command
             */
            try {
                line = is.readLine();
                os.println("GOTCHA");
                System.err.println(line);

                os.flush();
            } catch (IOException e) {
                System.out.println("IO Error/ Client " + line + " terminated abruptly");
                e.printStackTrace();
            } catch (NullPointerException e) {
//                line = this.getName(); //reused String line for getting thread name
                System.out.println("Client " + line + " Closed");
            } finally {
                try {
                    System.err.println("Connection Closing");
                    if (is != null) {
                        is.close();
                        System.out.println(" Socket Input Stream Closed");
                    }
                    if (os != null) {
                        os.close();
                        System.out.println("Socket Out Closed");
                    }
                    if (s != null) {
                        s.close();
                        System.out.println("Socket Closed");
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
    public GameServer(LocalState localState) {
        this.state = localState;
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
    public synchronized int start(int port) throws RemoteException {
        if (registry != null)
            throw new IllegalStateException("Server already running");
        Registry reg;
        if (port > 0) {
            reg = LocateRegistry.getRegistry(port);
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
        reg.rebind(state.name, state);
        return port;
    }

    /**
     * Stops the server by removing the game from the registry
     */
    public synchronized void stop() {
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

    public void parseRequest(RemoteState state, String request) {
        String[] tokens = request.split(" ");
        String username = tokens[0];
        String action = tokens[1];
    }


    /**
     * Command line program
     */
    public static void main(String[] args) throws Exception {
        int port = 0;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        String name = args[1];
        int width = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);
        int depth = Integer.parseInt(args[4]);
        int bhp = Integer.parseInt(args[5]);
        LocalState state = new LocalState(name, width, height, depth, bhp);
        GameServer server = new GameServer(state);
        try {
            port = server.start(port);
            Thread adminThread = new Thread();
            Thread[] clientThreads = new Thread[16];

            ExecutorService clientExecutor = Executors.newFixedThreadPool(16);
            Socket s = null;
            ServerSocket serverSocket = new ServerSocket(port);
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
