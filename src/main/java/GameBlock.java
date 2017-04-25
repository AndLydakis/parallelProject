import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class GameBlock {
    private int x;
    private int y;
    private int z;
    private int hp;
    private final int maxHp;
    private AtomicInteger shielded;
    private Player shielder;
    private final Object hpLock;
    private final Object shieldLock;

    public GameBlock(int x, int y, int z, int hp) {
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

    @Override
    public String toString() {
        return x + "_" + y + "_" + z;
    }

    /**
     * check if the
     * @return
     * @throws RemoteException
     */
    public int isShielded() throws RemoteException {
        return this.shielded.get();
    }

    /**
     * return the block's hitpoins
     * @return the block's hitpoints
     * @throws RemoteException
     */
    public int getHp() throws RemoteException {
        return this.hp;
    }

    public int attack(int dmg) throws RemoteException {
        synchronized (shieldLock) {
            synchronized (hpLock) {
                if (this.isShielded() >= dmg) {
                    int dmgBlocked = this.isShielded() - dmg;
                    shielder.gainCredits(dmgBlocked);
                    dmg -= (dmgBlocked);
                    if (dmg > 0) {
                        this.shielded.set(this.shielded.get() >= dmg ? this.shielded.get() - dmg : 0);
                        return (dmg);
                    }
                }else{
                    if(this.hp>dmg){
                        this.hp-=dmg;
                        return dmg;
                    }else{
                        int ret = this.hp;
                        this.hp = 0;
                        return ret;
                    }
                }
            }
        }
        System.err.println("Attack deflected");
        return -1;
    }

    /**
     * Restore some block hitpoints
     * @param rep a number of hit points to be
     * @return the amount of points that were repaired, so the player can gain the corresponding credits
     * @throws RemoteException
     */
    public int repair(int rep) throws RemoteException {
        synchronized (hpLock) {
            if (this.hp==maxHp) return 0;
            int r = (maxHp - this.hp) > rep?rep:maxHp - this.hp;
            this.hp = (this.hp+rep)>=maxHp?maxHp:(this.hp+rep);
            return r;
//            repairer.gainCredits((maxHp - this.hp) > rep?rep:maxHp - this.hp);
//            this.hp = (this.hp+rep)>maxHp?maxHp:(this.hp+rep);
        }
    }

    /**
     * Receive shield from player
     * @param p a player that is trying to shield a block
     * @param sp the shield points to be given to the block
     * @return true if the shield was placed successfully, false if otherwise
     * @throws RemoteException
     */
    public int shield(Player p, int sp) throws RemoteException {
        synchronized (shieldLock) {
            if (isShielded() == 0) {
                shielded.set(sp);
                shielder = p;
                return isShielded();
            }
        }
        return 0;
    }


}
