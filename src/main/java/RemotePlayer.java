import com.sun.org.apache.regexp.internal.RE;
import jdk.nashorn.internal.ir.Block;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/3/17.
 */
public interface RemotePlayer extends java.rmi.Remote {

    /**
     * @return the player's username
     * @throws RemoteException
     */
    String unameToString() throws RemoteException;

    /**
     * Update the status of the player
     * @throws RemoteException
     */
    void update(String s) throws RemoteException;

    /**
     *
     * @return a block given its coordinates
     * @throws RemoteException
     */
    RemoteBlock blockFromString() throws RemoteException;

    /**
     * @return the current level of the player
     * @throws RemoteException
     */
    int getLevel() throws RemoteException;

    /**
     * @return the ammount of credits at the player's disposal
     * @throws RemoteException
     */
    int getCredits() throws RemoteException;

    /**
     * @return the overall score of the player
     * @throws RemoteException
     */
    int getScore() throws RemoteException;

    /**
     * @return how much time must a player wait until he can boost again
     * @throws RemoteException
     */
    double getBoostCooldown() throws RemoteException;

    /**
     * @param c the ammount of credits to be added to the player's total (must be non-negative)
     * @throws RemoteException
     */
    void gainCredits(int c) throws RemoteException;

    /**
     * @param c the ammount of credits to be deducted from the player's account
     * @return true if the credits where successfully removed, false if the player did not have insufficient credits
     * @throws RemoteException
     */
    boolean removeCredits(int c) throws RemoteException;

    /**
     * @return a list of Blocks representing a face of the cube
     * @throws RemoteException
     */
    ArrayList<GameBlock> requestFace() throws RemoteException;
}
