import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface RemoteBlock extends java.rmi.Remote {

    /**
     * @return the ammount of shield points a block has
     * @throws RemoteException if rmi fails
     */
    int isShielded() throws RemoteException;

    /**
     * @return the hitpoints of the block
     * @throws RemoteException if rmi fails
     */
    int getHp() throws RemoteException;

    /**
     * @param dmg the power of the attack
     * @return credits equal to the amount of hit points removed
     * @throws RemoteException if rmi fails
     */
    int attack(int dmg) throws RemoteException;

    String toStringHp() throws RemoteException;

    /**
     * @param rep the hit points repaired
     * @return credits equal to the amount of hit points repaired
     * @throws RemoteException if rmi fails
     */
    int repair(int rep) throws RemoteException;

    /**
     * @param p  the Player attempting to shield the block
     * @param sp the Shield Power
     * @return true if the attempt succeeded, false if not
     * @throws RemoteException if rmi fails
     */
    boolean shield(Player p, int sp) throws RemoteException;
}
