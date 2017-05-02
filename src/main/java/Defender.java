import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Defenders are tasked with defending the game blocks
 * they can repair a single block, or shield it from future attacks
 */
public class Defender extends Player implements Serializable {

    private int shields;
    private int speed;
    private int repairRating;
    private long lastRepair;
    private long lastShield;
    private long lastBoost;
    private transient Object shieldLock;
    private int toLevelUpRr = 1;
    private int toLevelUpSpeed = 1;
    private int shieldPrice = 1;
    private int boostCost = 1;
    private int boostCooldown = 1;
    private int baseCooldown = 5;
    private volatile boolean boosted;

    /**
     * Constructor
     *
     * @param un username
     * @param s  score
     * @param cr credits
     * @throws RemoteException if rmi fails
     */
    Defender(String un, int s, int cr) throws RemoteException {
        super(un, 0, s, cr);
        synchronized (this) {
            this.speed = 1;
            this.repairRating = 1;
            this.lastRepair = -10000L;
            this.lastShield = -10000L;
            this.lastBoost = -10000L;
            this.shields = 0;
            this.boosted = false;
            this.shieldLock = new Object();
        }
    }

    /**
     * Constructor
     *
     * @param un      username
     * @param score   score
     * @param credits credits
     * @param repair  repair rating
     * @param speed   speed rating
     * @param items   number of shields
     * @throws RemoteException if rmi fails
     */
    Defender(String un, int score, int credits, int repair, int speed, int items) throws RemoteException {
        super(un, 1, score, credits);
        this.repairRating = repair;
        this.speed = speed;
        this.shields = items;
        this.lastRepair = -10000L;
        this.lastShield = -10000L;
        this.lastBoost = -10000L;
        this.boosted = false;
        this.shieldLock = new Object();
    }

    /**
     * Constructor from string
     *
     * @param s String s to create the user from
     * @throws RemoteException if rmi fails
     */
    public Defender(String s) throws RemoteException {
        super(s);
        this.speed = 1;
        this.repairRating = 1;
        this.lastRepair = -1L;
        this.lastShield = -1L;
        this.shields = 0;
        this.shieldLock = new Object();
    }

    /**
     * resets the lock to enable deserialization
     */
    void resetLock() {
        this.shieldLock = new Object();
    }

    /**
     * sets the repair rating of the player
     *
     * @param a the amount of repair rating to set the repair rating to
     */
    void setRepairRating(int a) {
        this.repairRating = a;
    }

    /**
     * sets the number of shields of the player
     *
     * @param a the number of shields of the player
     */
    void setShields(int a) {
        this.shields = a;
    }

    /**
     * sets the speed of the player
     *
     * @param a the speed to set the speed to
     */
    void setSpd(int a) {
        this.speed = a;
    }

    /**
     * sets the amount necessary need to level up repair rating
     *
     * @param a the amount necessary need to level up repair rating
     */
    void setLevelRr(int a) {
        this.toLevelUpRr = a;
    }

    /**
     * sets the amount necessary need to level up speed
     *
     * @param a the amount necessary need to level up speed
     */
    void setLevelSpd(int a) {
        this.toLevelUpSpeed = a;
    }

    /**
     * Update player from a socket response
     *
     * @param s String formatted as "SCORE CREDITS LEVEL SPEED REPAIR_RATING SHIELDS"
     */
    public void update(String s) throws RemoteException {
        super.update(s);
        String[] tokens = s.split(" ");
        this.speed = Integer.parseInt(tokens[3]);
        this.repairRating = Integer.parseInt(tokens[4]);
        this.shields = Integer.parseInt(tokens[5]);
    }

    /**
     * Returns the player inf as a string
     *
     * @return a string formatted as USERNAME SCORE CREDITS SPEED REPAIR_RATING SHIELDS SPEED TOLVRR TOLVLSPD
     * @throws RemoteException if rmi fails
     */
    public String playerToString() throws RemoteException {
        return (unameToString() + " " + getScore() + " " + getCredits() + " " + speed + " " + shields + " " + repairRating + " " + toLevelUpRr + " " + toLevelUpSpeed);
    }

    /**
     * print the defender as a string
     *
     * @return a string containing the defender's stats
     * @throws RemoteException if rmi fails
     */
    public String print() throws RemoteException {
        return super.print() +
                "Role: Defender\n" +
                "Speed: " + speed + "\n" +
                "Repair Rating: " + repairRating + "\n" +
                "Shields Available: " + shields;
    }

    /**
     * Return the speed of the player
     *
     * @return the speed of the player
     * @throws RemoteException if rmi fails
     */
    public double getSpeed() throws RemoteException {
        return this.speed;
    }

    /**
     * Return the repair rating of the player
     *
     * @return the repair rating of the player
     */
    private int getRepairRating() throws RemoteException {
        return this.repairRating;
    }

    /**
     * Return the number of shields available to the player
     *
     * @return the number of shields available to the player
     */
    private int getShields() throws RemoteException {
        return this.shields;
    }

    /**
     * Returns true if the player can place another shield, false otherwise
     *
     * @return true if the player can place another shield, false otherwise
     * @throws RemoteException if rmi fails
     */
    public boolean canShield() throws RemoteException {
        return ((System.nanoTime() - this.lastRepair) > this.speed);
    }

    /**
     * Return true if the player can repair a block, false otherwise
     *
     * @return true if the player can repair a block, false otherwise
     * @throws RemoteException if rmi fails
     */
    private boolean canRepair() throws RemoteException {
        resetBoost();
        if (boosted) {
            return (System.nanoTime() - this.lastRepair) / 1e9 + 10 * this.speed > baseCooldown;
        } else {
            return (System.nanoTime() - this.lastRepair) / 1e9 + this.speed > baseCooldown;
        }
    }

    /**
     * @return true if the player can boost his cooldowns, false otherwise
     * @throws RemoteException if rmi fail
     */
    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost) > getBoostCooldown());
    }


    /**
     * Increase the repair rating if the player has enough credits
     *
     * @return true if the player had enough credits to level up repair rating, false otherwise
     * @throws RemoteException if rmi fails
     */
    int levelUpRr() throws RemoteException {
        int cr = getCredits();
        if (super.removeCredits(toLevelUpRr)) {
            repairRating += 1;
            toLevelUpRr *= 2;
            return repairRating;
        }
        System.err.println("Need " + toLevelUpRr + " credits to level up repair rating, current credits: " + cr);
        return -toLevelUpRr;
    }

    /**
     * Increase the speed of the if the player has enough credits
     *
     * @return true if the player had enough credits to level up speed, false otherwise
     * @throws RemoteException if rmi fails
     */
    int levelUpSpeed() throws RemoteException {
        if (((System.nanoTime() - this.lastBoost) > this.speed)) {
            if (super.removeCredits(toLevelUpSpeed)) {
                speed += 1;
                toLevelUpSpeed *= 2;
                return speed;
            }
        }
//        System.err.println("Need " + toLevelUpSpeed + " credits to level up speed, current credits: " + cr);
        return -toLevelUpSpeed;
    }

    /**
     * Restore a block's hitpoints for an amount equal to the players repair rating
     *
     * @param b the block to be repaired
     * @return true if the block was repaired, false if the block was already destoryed
     * @throws RemoteException if rmi fails
     */
    int repair(GameBlock b) throws RemoteException {
        if (!canRepair()) return 0;
        int p = b.repair(getRepairRating());
        if (p >= 0) {
            this.gainCredits(p);
            lastRepair = System.nanoTime();
        }
        return p;
    }


    int shield(GameBlock b) throws RemoteException {
        if (!canRepair()) return 0;
        synchronized (shieldLock) {
            if (this.getShields() > 0) {
                int res = b.shield(this, this.getRepairRating() * 5);
                if (res > 0) {
                    shields--;
                    lastRepair = System.nanoTime();
                }
                return res;
            }
        }
        return 0;
    }

    /**
     * Increases the number of available shields if the player has enough credits
     *
     * @return the number of shields available to the player
     * @throws RemoteException if rmi fails
     */
    int buyShield() throws RemoteException {
        synchronized (this.shieldLock) {
            if (super.removeCredits(shieldPrice)) {
                shields++;
                return shields;
            } else {
                System.err.println("Not enough credits to buy a shield, " + shieldPrice + " credits needed");
                return -shieldPrice;
            }
        }
    }

    /**
     * Temporarily increase the player's speed if he has sufficient credits, and his boost is not in cooldown
     *
     * @return true if the boost succeeded, false otherwise
     * @throws RemoteException if rmi fails
     */
    synchronized int boost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost) / 1e9 > boostCooldown) {
            if (super.removeCredits(boostCost)) {
//            this.speed = this.speed * 2;
                lastBoost = System.nanoTime();
                this.boosted = true;
                return 1;
            }
        }
        return 0;
    }

    /**
     * Reset the player's speed back to the original pre-boost value
     *
     * @throws RemoteException if rmi fails
     */
    private synchronized void resetBoost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost) / 1e9 > boostCooldown * 10) {
            boosted = false;
//            System.err.println("Boost reset");
        }
    }
}

