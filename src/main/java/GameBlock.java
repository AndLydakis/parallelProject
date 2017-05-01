import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lydakis-local on 4/2/17.
 * Blocks make up the game cube
 * the have x,y,z coordinates,
 * a number of hitpoints
 * an amount of shielding
 * a shielder if the previous amount is not 0
 */
public class GameBlock implements Serializable {
    private int x;
    private int y;
    private int z;
    private int hp;
    private final int maxHp;
    private AtomicInteger shielded;
    private Player shielder;
    private transient Object hpLock;
    private transient Object shieldLock;

    /**
     * Constructor
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @param z  z coordinate
     * @param hp hitpoints
     */
    GameBlock(int x, int y, int z, int hp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        this.maxHp = hp;
        this.shielded = new AtomicInteger(0);
        this.shielder = null;
        this.hpLock = new Object();
        this.shieldLock = new Object();
    }

    /**
     * resets the lock to enable deserialization
     */
    void resetLock() {
        this.hpLock = new Object();
        this.shieldLock = new Object();
    }

    /**
     * @return the block as a X_Y_Z string
     */
    @Override
    public String toString() {
        try {
            return x + "_" + y + "_" + z;
        } catch (Exception e) {
            return "";
        }
    }

    String toStringHp() {
        try {
            return x + "_" + y + "_" + z + ":" + getHp();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if the block is shielded and return the amount of shielding
     *
     * @return the amount of shielding available to the block
     * @throws RemoteException if rmi fails
     */
    private int isShielded() throws RemoteException {
        return this.shielded.get();
    }

    /**
     * Return the block's hitpoins
     *
     * @return the block's hitpoints
     * @throws RemoteException if rmi fails
     */
    int getHp() throws RemoteException {
        return this.hp;
    }

    /**
     * Attacks the block
     *
     * @param dmg the damage that is attempted
     * @return the amount of actual damage done to the block
     * so the player can gain the corresponding credits
     * @throws RemoteException if rmi fails
     */
    int attack(int dmg) throws RemoteException {
        synchronized (shieldLock) {
            synchronized (hpLock) {
                if (this.hp <= 0) return 0;
                if (this.isShielded() > 0) {
                    int dmgBlocked = 0;
                    if (this.isShielded() > 0) {
                        dmgBlocked = this.shielded.get() >= dmg ? dmg : this.shielded.get();
                        shielder.gainCredits(dmgBlocked);
                        shielded.set(shielded.get() - dmgBlocked);
                        if (shielded.get() == 0) {
                            shielder = null;
                        }
                    }
                    System.err.println("Damage blocked : " + dmgBlocked);
                    dmg -= (dmgBlocked);
                    System.err.println("Damage dealt : " + dmg);
                }
                if (this.hp > dmg) {
                    this.hp -= dmg;
                    return dmg;
                } else {
                    int ret = this.hp;
                    this.hp = 0;
                    return ret;
                }
            }
        }
    }

    /**
     * Restore some block hitpoints
     *
     * @param rep a number of hit point to be restpored
     * @return the amount of points that were repaired,
     * so the player can gain the corresponding credits
     * @throws RemoteException if rmi fails
     */
    int repair(int rep) throws RemoteException {
        synchronized (hpLock) {
            if (this.hp <= 0) return 0;
            if (this.hp == maxHp) return 0;
            int r = (maxHp - this.hp) > rep ? rep : maxHp - this.hp;
            this.hp = (this.hp + rep) >= maxHp ? maxHp : (this.hp + rep);
            return r;
//            repairer.gainCredits((maxHp - this.hp) > rep?rep:maxHp - this.hp);
//            this.hp = (this.hp+rep)>maxHp?maxHp:(this.hp+rep);
        }
    }

    /**
     * Receive shield from player
     *
     * @param p  a player that is trying to shield a block
     * @param sp the shield points to be given to the block
     * @return true if the shield was placed successfully, false if otherwise
     * @throws RemoteException if rmi fails
     */
    int shield(Player p, int sp) throws RemoteException {
        synchronized (shieldLock) {
            if (isShielded() == 0) {
                shielded.set(sp);
                shielder = p;
                return isShielded();
            }
        }
        return -1;
    }


}
