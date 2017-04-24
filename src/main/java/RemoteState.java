import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface RemoteState extends java.rmi.Remote {
    RemotePlayer login(String username) throws RemoteException;

    boolean logout(String username) throws RemoteException;

    boolean register(String username, int role) throws RemoteException;

    boolean requestPrimary(String user, int role, String block) throws RemoteException;

    boolean requestSecondary(String user, int role, String block) throws RemoteException;

    boolean requestBoost(String user) throws RemoteException;

    boolean levelPrimary(String user, int role) throws RemoteException;

    boolean levelSecondary(String user, int role) throws RemoteException;

    boolean buy(String user, int role) throws RemoteException;

    String getTargets();

    String parseRequest(String req) throws RemoteException;
}
