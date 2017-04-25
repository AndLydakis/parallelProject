import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/25/17.
 */
public interface ClientInterface {
    void repeat() throws RemoteException;

    void testEnd() throws RemoteException;

    void initialMenu() throws RemoteException;

    void AttackerMenu() throws RemoteException;

    void processAttackerOptions() throws RemoteException;

    void DefenderMenu() throws RemoteException;

    void processDefenderOptions() throws RemoteException;

    void actionMenu() throws RemoteException;

    void Register() throws RemoteException;

    void login() throws RemoteException;

    void logout() throws RemoteException;
}
