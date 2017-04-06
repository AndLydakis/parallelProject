import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
/**
 * Created by lydakis-local on 4/4/17.
 */
public class GameServerSocket {
    private final LocalState state;
    private Registry registry;

    /**
     * create a server for the given game
     */
    public GameServerSocket(LocalState localState) {
        this.state = localState;
    }

    /**
     *
     * @param port
     * @return the port if connection was successful, -1 if else
     * @throws RemoteException
     */
    public synchronized int start(int port) throws RemoteException {
        if (registry != null)
            throw new IllegalStateException("Server already running");
        Registry reg;
        try {
            reg = LocateRegistry.getRegistry(port);
        }catch (Exception e){
            System.err.println("Could not connect to server");
            e.printStackTrace();
        }
        return port;
    }
}
