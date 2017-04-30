import jdk.nashorn.internal.ir.Block;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/2/17.
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

    public Defender(String s) throws RemoteException {
        super(s);
        this.speed = 1;
        this.repairRating = 1;
        this.lastRepair = -1l;
        this.lastShield = -1l;
        this.shields = 0;
        this.shieldLock = new Object();
    }

    public void resetLock(){
        this.shieldLock = new Object();
    }
    public void setRepairRating(int a){
        this.repairRating= a;
    }
    public void setShields(int a){
        this.shields= a;
    }
    public void setSpd(int a){
        this.speed = a;
    }
    public void setLevelRr(int a){
        this.toLevelUpRr = a;
    }
    public void setLevelSpd(int a){
        this.toLevelUpSpeed= a;
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
     * @throws RemoteException
     */
    public String playerToString() throws RemoteException {
        return (unameToString() + " " + getScore() + " " + getCredits() + " " + speed + " " + shields + " " + repairRating + " " + toLevelUpRr + " " + toLevelUpSpeed);
    }

    public String print() throws RemoteException {
        return super.print() +
                "Speed: " + speed + "\n" +
                "Repair Rating: " + repairRating + "\n" +
                "Shields Available: " + shields;

    }

    /**
     * Return the speed of the player
     *
     * @return
     */
    public double getSpeed() throws RemoteException {
        return this.speed;
    }

    /**
     * Return the repair rating of the player
     *
     * @return the repair rating of the player
     */
    public int getRepairRating() throws RemoteException {
        return this.repairRating;
    }

    /**
     * Return the number of shields available to the player
     *
     * @return the number of shields available to the player
     */
    public int getShields() throws RemoteException {
        return this.shields;
    }

    /**
     * Returns true if the player can place another shield, false otherwise
     *
     * @return true if the player can place another shield, false otherwise
     */
    public boolean canShield() throws RemoteException {
        return ((System.nanoTime() - this.lastRepair) > this.speed);
    }

    /**
     * Return true if the player can repair a block, false otherwise
     *
     * @return true if the player can repair a block, false otherwise
     */
    public boolean canRepair() throws RemoteException {
        resetBoost();
        if (boosted) {
            return (System.nanoTime() - this.lastRepair) / 1e9 + 10 * this.speed > baseCooldown;
        } else {
            return (System.nanoTime() - this.lastRepair) / 1e9 + this.speed > baseCooldown;
        }
    }

    /**
     * @return true if the player can boost his cooldowns, false otherwise
     */
    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost) > getBoostCooldown());
    }


    /**
     * Increase the repair rating if the player has enough credits
     *
     * @return true if the player had enough credits to level up repair rating, false otherwise
     * @throws RemoteException
     */
    public int levelUpRr() throws RemoteException {
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
     * @throws RemoteException
     */
    public int levelUpSpeed() throws RemoteException {
        int cr = getCredits();
        if (((System.nanoTime() - this.lastBoost) > this.speed)) {
            if (super.removeCredits(toLevelUpSpeed)) {
                speed += 1;
                toLevelUpSpeed *= 2;
                return speed;
            }
        }
        System.err.println("Need " + toLevelUpSpeed + " credits to level up speed, current credits: " + cr);
        return -toLevelUpSpeed;
    }

    /**
     * Restore a block's hitpoints for an amount equal to the players repair rating
     *
     * @param b the block to be repaired
     * @return true if the block was repaired, false if the block was already destoryed
     * @throws RemoteException if rmi fails
     */
    public int repair(GameBlock b) throws RemoteException {
        if (!canRepair()) return 0;
        int p = b.repair(getRepairRating());
        if (p >= 0) {
            this.gainCredits(p);
            lastRepair = System.nanoTime();
        }
        return p;
    }


    public int shield(GameBlock b) throws RemoteException {
        if (!canRepair()) return 0;
        synchronized (shieldLock) {
            if (this.getShields() > 0) {
                shields--;
                lastRepair = System.nanoTime();
                return b.shield(this, this.getRepairRating() * 5);
            }
        }
        return 0;
    }

    /**
     * Increases the number of available shields if the player has enough credits
     *
     * @return the number of shields available to the player
     * @throws RemoteException
     */
    public int buyShield() throws RemoteException {
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
     * @throws RemoteException
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
     * @throws RemoteException
     */
    synchronized public void resetBoost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost)/1e9 > boostCooldown*10) {
            boosted = false;
            System.err.println("Boost reset");
        }
    }
}

