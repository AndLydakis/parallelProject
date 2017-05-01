import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface RemoteState extends java.rmi.Remote {
    RemotePlayer login(String username) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    boolean register(String username, int role) throws RemoteException;

    boolean register(String username, int role, int score, int credits, int primary, int secondary, int items) throws RemoteException;

    boolean isAlive() throws RemoteException;

    int requestPrimary(String user, int role, String block) throws RemoteException;

    int requestSecondary(String user, int role, String block) throws RemoteException;

    int requestBoost(String user, int role) throws RemoteException;

    int levelPrimary(String user, int role) throws RemoteException;

    int levelSecondary(String user, int role) throws RemoteException;

    int buy(String user, int role) throws RemoteException;

    int printStatus() throws RemoteException;

    String getTargets() throws RemoteException;

    String printPlayer(String player, int r) throws RemoteException;

    String parseRequest(String req) throws RemoteException;

    String printLeaderBoards() throws RemoteException;
}
