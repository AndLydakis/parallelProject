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
        this.shielded = new AtomicInteger(-1);
        this.shielder = null;
        this.hpLock = new Object();
        this.shieldLock = new Object();
    }

    @Override
    public String toString() {
        return x + "_" + y + "_" + "_" + z;
    }

    public int isShielded() throws RemoteException {
        return this.shielded.get();
    }

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
                }
            }
        }
        return 0;
    }

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

    public boolean shield(Player p, int sp) throws RemoteException {
        synchronized (shieldLock) {
            if (isShielded() == 0) {
                shielded.set(sp);
                shielder = p;
                return true;
            }
        }
        return false;
    }


}
