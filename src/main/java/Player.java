import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Player extends UnicastRemoteObject implements Comparable<Player>, RemotePlayer{
    final String userName;
    private int score;
    private int credits;
    private int level;
    private final Object creditLock;
    private final Object logLock;
    private volatile boolean logged;

    private final double boostCooldown = 10.0;

    public Player(String un, int s, int cr) throws RemoteException {
        this.userName = un;
        this.score = s;
        this.credits = cr;
        this.level = 1;
        this.creditLock = new Object();
        this.logLock = new Object();
        this.logged = false;
    }

    public String toString() {
        return this.userName;
    }

    public void login() throws RemoteException{
        synchronized (logLock) {
            logged = true;
        }
    }

    public void logout() throws RemoteException{
        synchronized (logLock) {
            logged = false;
        }
    }

    public int getLevel()throws RemoteException{
        return this.level;
    }

    public int getCredits()throws RemoteException{
        return this.credits;
    }

    public int getScore()throws RemoteException{
        return this.score;
    }

    public double getBoostCooldown()throws RemoteException{
        return this.boostCooldown;
    }

    public void gainCredits(int c)throws RemoteException{
        synchronized (creditLock) {
            this.credits += c;
            this.score += c;
        }
    }

    public boolean removeCredits(int c)throws RemoteException{
        synchronized (creditLock) {
            if(this.credits>=c){
                credits -= c;
                return true;
            }
        }
        return false;
    }

    public ArrayList<GameBlock> requestFace()throws RemoteException{
        return  new ArrayList<>();
    }

    @Override
    public int compareTo(Player o) {
        return this.score-o.score;
    }
}
