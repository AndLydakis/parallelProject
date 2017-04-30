import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Player extends UnicastRemoteObject implements Comparable<Player>, RemotePlayer, Serializable {
    final String userName;
    private int score;
    private int credits;
    private int level;
    private final int role;
    private transient Object creditLock;
    private transient Object logLock;
    private volatile boolean logged;
    ConcurrentHashMap<String, RemoteBlock> targets;

    private final double boostCooldown = 10.0;

    /**
     * Create player from string for the socket service
     *
     * @param un username
     * @param score  score
     * @param credits credits
     * @throws RemoteException
     */
    public Player(String un, int role, int score, int credits) throws RemoteException {
        this.userName = un;
        this.score = score;
        this.credits = credits;
        this.level = 1;
        this.creditLock = new Object();
        this.logLock = new Object();
        this.logged = false;
        this.role = role;
    }

    public void resetLocks(){
        creditLock = new Object();
        logLock = new Object();
    }
    public int getRole() throws RemoteException {
        return this.role;
    }

    /**
     * Load player from string for the socket service
     *
     * @param s String formatted as "USERNAME SCORE CREDITS LEVEL"
     * @throws RemoteException
     */
    public Player(String s) throws RemoteException {
        String[] tokens = s.split(" ");
        this.userName = tokens[0];
        this.role = Integer.parseInt(tokens[1]);
        this.score = Integer.parseInt(tokens[2]);
        this.credits = Integer.parseInt(tokens[3]);
        this.creditLock = new Object();
        this.logLock = new Object();
        this.logged = false;
    }

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
        this.level = Integer.parseInt(tokens[2]);
    }

    public void assignTargets(ConcurrentHashMap map) throws RemoteException {
        this.targets = map;
    }

    public RemoteBlock blockFromString(String coords) throws RemoteException {
        return targets.get(coords);
    }

    public String unameToString() throws RemoteException {
        return this.userName;
    }

    @Override
    public RemoteBlock blockFromString() throws RemoteException {
        return null;
    }

    public boolean login() throws RemoteException {
        synchronized (logLock) {
            if (!logged) {
                logged = true;
                return true;
            }
            return false;
        }
    }

    public boolean logout() throws RemoteException {
        synchronized (logLock) {
            if (logged) {
                logged = false;
                return true;
            }
            return false;
        }
    }

    public int getLevel() throws RemoteException {
        return this.level;
    }

    public int getCredits() throws RemoteException {
        return this.credits;
    }

    public int getScore() throws RemoteException {
        return this.score;
    }

    public double getBoostCooldown() throws RemoteException {
        return this.boostCooldown;
    }

    public void gainCredits(int c) throws RemoteException {
        synchronized (creditLock) {
            this.credits += c;
            this.score += c;
        }
    }

    public boolean removeCredits(int c) throws RemoteException {
        synchronized (creditLock) {
            if (this.credits >= c) {
                credits -= c;
                return true;
            }
        }
        return false;
    }

    public ArrayList<GameBlock> requestFace() throws RemoteException {
        return new ArrayList<>();
    }

    @Override
    public int compareTo(Player o) {
        return o.score - this.score;
    }
}
