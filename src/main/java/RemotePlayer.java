import com.sun.org.apache.regexp.internal.RE;
import jdk.nashorn.internal.ir.Block;

import java.io.Serializable;
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
     * @throws RemoteException if rmi fails
     */
    void update(String s) throws RemoteException;

    /**
     *
     * @return a block given its coordinates
     * @throws RemoteException if rmi fails
     */
    RemoteBlock blockFromString() throws RemoteException;

    /**
     * @return the current level of the player
     * @throws RemoteException if rmi fails
     */
    int getLevel() throws RemoteException;

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
     * @return a list of Blocks representing a face of the cube
     * @throws RemoteException if rmi fails
     */
    ArrayList<GameBlock> requestFace() throws RemoteException;
}
