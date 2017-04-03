import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface State extends java.rmi.Remote{
    Player login(String username) throws RemoteException ;

    boolean logout(String username) throws RemoteException ;

    boolean register(String username, int role) throws RemoteException;
}
