import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface RemoteBlock extends java.rmi.Remote{

    /**
     *
     * @return the ammount of shield points a block has
     * @throws RemoteException
     */
    public int isShielded() throws RemoteException ;

    /**
     *
     * @return the hitpoints of the block
     * @throws RemoteException
     */
    public int getHp() throws RemoteException;

    /**
     *
     * @param attacker the Player who attacked
     * @param dmg the power of the attack
     * @return a Record of the form <PLAYER> DAMAGES <BLOCK> FOR <DAMAGE>
     * @throws RemoteException
     */
    Record attack(Player attacker, int dmg)throws RemoteException ;

    /**
     *
     * @param repairer the Player who repaired
     * @param rep the repair power
     * @return a Record of the form <PLAYER> REPAIRS <BLOCK> FOR <POINTS>
     * @throws RemoteException
     */
    Record repair(Player repairer, int rep)throws RemoteException ;

    /**
     *
     * @param p the Player attempting to shield the block
     * @param sp the Shield Power
     * @return true if the attempt succeeded, false if not
     * @throws RemoteException
     */
    public boolean shield(Player p, int sp) throws RemoteException ;
}
