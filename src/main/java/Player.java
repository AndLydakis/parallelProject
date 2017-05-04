import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic player class maintains a username, the player's score, credits
 * and role
 */
public class Player extends UnicastRemoteObject implements Comparable<Player>, RemotePlayer, Serializable {
    private final String userName;
    private int score;
    private int credits;
    private final int role;
    private transient Object creditLock;
    private transient Object logLock;
    private volatile boolean logged;

    private final double boostCooldown = 10.0;

    /**
     * Create player from string for the socket service
     *
     * @param un      username
     * @param score   score
     * @param credits credits
     * @throws RemoteException if rmi fails
     */
    Player(String un, int role, int score, int credits) throws RemoteException {
        this.userName = un;
        this.score = score;
        this.credits = credits;
        this.creditLock = new Object();
        this.logLock = new Object();
        this.logged = false;
        this.role = role;
    }

    /**
     * Reset locks to enable deserialization
     */
    void resetLocks() {
        creditLock = new Object();
        logLock = new Object();
    }

    /**
     * Get the role of a player
     *
     * @return 1 if the player is an attacker, 0 otherwise
     * @throws RemoteException if rmi fails
     */
    public int getRole() throws RemoteException {
        return this.role;
    }

    /**
     * Load player from string for the socket service
     *
     * @param s String formatted as "USERNAME SCORE CREDITS LEVEL"
     * @throws RemoteException if rmi fails
     */
    Player(String s) throws RemoteException {
        String[] tokens = s.split(" ");
        this.userName = tokens[0];
        this.role = Integer.parseInt(tokens[1]);
        this.score = Integer.parseInt(tokens[2]);
        this.credits = Integer.parseInt(tokens[3]);
        this.creditLock = new Object();
        this.logLock = new Object();
        this.logged = false;
    }

    /**
     * Return the player  as a string
     *
     * @return the player  as a string
     * @throws RemoteException if rmi fails
     */
    public String print() throws RemoteException {
        return "Username: " + userName + "\n" +
                "Score: " + score + "\n" +
                "Credits: " + credits + "\n";
    }

    /**
     * Update the player from a server response
     *
     * @param s String formatted as "SCORE CREDITS LEVEL"
     */
    public void update(String s) throws RemoteException {
        String[] tokens = s.split(" ");
        this.score = Integer.parseInt(tokens[0]);
        this.credits = Integer.parseInt(tokens[1]);
    }

    /**
     * return the player's username
     *
     * @return the username of the player
     * @throws RemoteException if rmi fails
     */
    public String unameToString() throws RemoteException {
        return this.userName;
    }


    @Override
    public RemoteBlock blockFromString() throws RemoteException {
        return null;
    }

    /**
     * Log in if not already logged in
     *
     * @return true if the log in was successful, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean login() throws RemoteException {
        synchronized (logLock) {
            if (!logged) {
                logged = true;
                return true;
            }
            return false;
        }
    }

    /**
     * Log in out not already logged out
     *
     * @return true if the log out was successful, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean logout() throws RemoteException {
        synchronized (logLock) {
            if (logged) {
                logged = false;
                return true;
            }
            return false;
        }
    }

    /**
     * Return the player's credits
     *
     * @return the player's credits
     * @throws RemoteException if rmi fails
     */
    public int getCredits() throws RemoteException {
        return this.credits;
    }

    /**
     * Return the player's score
     *
     * @return the player's score
     * @throws RemoteException if rmi fails
     */
    public int getScore() throws RemoteException {
        return this.score;
    }

    /**
     * Return the player's boost cooldown
     *
     * @return the player's boost cooldown
     * @throws RemoteException if rmi fails
     */
    public double getBoostCooldown() throws RemoteException {
        return this.boostCooldown;
    }

    /**
     * Add a number of credits to the player's total
     *
     * @param c the amount of credits to be added to the player's total (must be non-negative)
     * @throws RemoteException if rmi fails
     */
    public void gainCredits(int c) throws RemoteException {
        synchronized (creditLock) {
            this.credits += c;
            this.score += c;
        }
    }

    /**
     * Remove a number of credits from the player's total
     *
     * @param c the amount of credits to be removed from
     *          the player's total (must be non-negative)
     * @throws RemoteException if rmi fails
     */
    public boolean removeCredits(int c) throws RemoteException {
        synchronized (creditLock) {
            if (this.credits >= c) {
                credits -= c;
                return true;
            }
        }
        return false;
    }

    @Override
    public int boost() throws RemoteException {
        return 0;
    }

    @Override
    public int upgradePrimary() throws RemoteException {
        return 0;
    }

    @Override
    public int upgradeSecondary() throws RemoteException {
        return 0;
    }

    @Override
    public int buyItem() throws RemoteException {
        return 0;
    }

    @Override
    public void setPrimary(int a) throws RemoteException {

    }

    @Override
    public void setSecondary(int a) throws RemoteException {

    }

    @Override
    public void setLevelPrimary(int a) throws RemoteException {

    }

    @Override
    public void setLevelSecondary(int a) throws RemoteException {

    }

    @Override
    public void setItems(int a) throws RemoteException {

    }

    /**
     * request the cube's face
     *
     * @return the cube's face as an ArrayList of GameBlocks
     * @throws RemoteException if rmi fails
     */
    public ArrayList<GameBlock> requestFace() throws RemoteException {
        return new ArrayList<>();
    }

    /**
     * Comparator for players
     *
     * @param o other player
     * @return the difference in score between the two players
     */
    @Override
    public int compareTo(Player o) {
        return o.score - this.score;
    }
}
