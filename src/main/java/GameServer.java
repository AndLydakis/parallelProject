import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class GameServer {
    public static final String SERVER_NAME = "AnimeWasAMistake";
    private final LocalState state;
    private Registry registry;

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
            int tries =0 ;
            while(true){
                port = 50000 + rand.nextInt(10000);
                try{
                    reg = LocateRegistry.createRegistry(port);
                    break;
                }catch (RemoteException re){
                    if(++tries < 10 && re.getCause() instanceof java.net.BindException)
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
    public synchronized void stop(){
        if(registry!=null){
            try {
                registry.unbind(state.name);
            }catch (Exception e){
                System.err.printf("unable to stop: %s%n", e.getMessage());
            }finally {
                registry = null;
            }
        }
    }

    /**
     * Command line program
     */
    public static void main(String[] args) throws Exception{
        int port = 0;
        if(args.length>0){
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
        }catch (RemoteException re){
            Throwable t = re.getCause();
            if(t instanceof java.net.ConnectException){
                System.err.println("Unable to connecto to registry: "+re.getMessage());
            }else if(t instanceof java.net.BindException){
                System.err.println("Cannot start registry: "+re.getMessage());
            }else{
                System.err.println("Cannot start server: "+ re.getMessage());
            }
            UnicastRemoteObject.unexportObject(state, false);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

}
