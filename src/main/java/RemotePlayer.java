import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Remote Interface for player objects
 */
public interface RemotePlayer extends java.rmi.Remote {

    /**
     * @return the player's username
     * @throws RemoteException if rmi fails
     */
    String unameToString() throws RemoteException;

    /**
     * @return a description of the player
     * @throws RemoteException if rmi fails
     */
    String print() throws RemoteException;

    /**
     * Update the status of the player
     *
     * @param s a String containing th new values for the player stats
     * @throws RemoteException if rmi fails
     */
    void update(String s) throws RemoteException;

    /**
     * @return a block given its coordinates
     * @throws RemoteException if rmi fails
     */
    RemoteBlock blockFromString() throws RemoteException;

    /**
     * Return the role of the player
     *
     * @return int representation of the player's role
     * @throws RemoteException if rmi fails
     */
    int getRole() throws RemoteException;

    /**
     * @return the ammount of credits at the player's disposal
     * @throws RemoteException if rmi fails
     */
    int getCredits() throws RemoteException;

    /**
     * @return the overall score of the player
     * @throws RemoteException if rmi fails
     */
    int getScore() throws RemoteException;

    /**
     * @return how much time must a player wait until he can boost again
     * @throws RemoteException if rmi fails
     */
    double getBoostCooldown() throws RemoteException;

    /**
     * @param c the ammount of credits to be added to the player's total (must be non-negative)
     * @throws RemoteException if rmi fails
     */
    void gainCredits(int c) throws RemoteException;

    /**
     * @param c the ammount of credits to be deducted from the player's account
     * @return true if the credits where successfully removed, false if the player did not have insufficient credits
     * @throws RemoteException if rmi fails
     */
    boolean removeCredits(int c) throws RemoteException;

    /**
     * Temporarily boost the player's speed
     *
     * @return 1 if the boost succeeded, 0 otherwise
     * @throws RemoteException if rmi fails
     */
    int boost() throws RemoteException;

    /**
     * Upgrade the player's primary attribute
     *
     * @return the new attribute value, or the credits needed to upgrade
     * @throws RemoteException if rmi fails
     */
    int upgradePrimary() throws RemoteException;

    /**
     * Upgrade the player's secondary attribute
     *
     * @return the new attribute value, or the credits needed to upgrade
     * @throws RemoteException if rmi fails
     */
    int upgradeSecondary() throws RemoteException;

    /**
     * Buy an item (bomb/shield)if there are enough credits
     *
     * @return the new number of available items, or the credits needed to buy on
     * @throws RemoteException if rmi fails
     */
    int buyItem() throws RemoteException;

    void setPrimary(int a) throws RemoteException;

    void setSecondary(int a) throws RemoteException;

    void setLevelPrimary(int a) throws RemoteException;

    void setLevelSecondary(int a) throws RemoteException;

    /**
     * Sets the number of available items
     *
     * @throws RemoteException if rmi fails
     */
    void setItems(int a) throws RemoteException;
}
