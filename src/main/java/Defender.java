import jdk.nashorn.internal.ir.Block;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Defender extends Player {

    int shields;
    double speed;
    int repairRating;
    long lastRepair;
    long lastShield;
    private long lastBoost;
    private int toLevelUpRr = 10;
    private int toLevelUpSpeed = 10;
    private final int shieldPrice = 500;
    private volatile boolean boosted;

    public Defender(String un, int s, int cr) throws RemoteException {
        super(un, s, cr);
        synchronized (this) {
            this.speed = 1.0;
            this.repairRating = 1;
            this.lastRepair = -10000L;
            this.lastShield = -10000L;
            this.shields = 0;
            this.boosted = false;
        }
    }

    public Defender(String s) throws RemoteException {
        super(s);
        this.speed = 1.0;
        this.repairRating = 1;
        this.lastRepair = -1l;
        this.lastShield = -1l;
        this.shields = 0;
    }

    /**
     * Update player from a socket response
     *
     * @param s String formatted as "SCORE CREDITS LEVEL SPEED REPAIR_RATING SHIELDS"
     */
    public void update(String s) throws RemoteException {
        super.update(s);
        String[] tokens = s.split(" ");
        this.speed = Double.parseDouble(tokens[3]);
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
     * @return the repair rating of the player
     */
    public int getRepairRating() throws RemoteException {
        return this.repairRating;
    }

    /**
     * Return the number of shields available to the player
     * @return the number of shields available to the player
     */
    public int getShields() throws RemoteException {
        return this.shields;
    }

    /**
     * Returns true if the player can place another shield, false otherwise
     * @return true if the player can place another shield, false otherwise
     */
    public boolean canShield() throws RemoteException {
        return ((System.nanoTime() - this.lastRepair) > this.speed);
    }

    /**
     * Return true if the player can repair a block, false otherwise
     * @return true if the player can repair a block, false otherwise
     */
    public boolean canRepair() throws RemoteException {
        return ((System.nanoTime() - this.lastRepair) > this.speed);
    }

    /**
     * @return true if the player can boost his cooldowns, false otherwise
     */
    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost) > getBoostCooldown());
    }


    /**
     * Increase the repair rating if the player has enough credits
     * @return true if the player had enough credits to level up repair rating, false otherwise
     * @throws RemoteException
     */
    public boolean levelUpRr() throws RemoteException {
        int cr = getCredits();
        if (super.removeCredits(toLevelUpRr)) {
            repairRating += 1;
            toLevelUpRr *= 10;
            return true;
        }
        System.err.println("Need " + toLevelUpRr + " credits to level up repair rating, current credits: " + cr);
        return false;
    }

    /**
     * Increase the speed of the if the player has enough credits
     *
     * @return true if the player had enough credits to level up speed, false otherwise
     * @throws RemoteException
     */
    public boolean levelUpSpeed() throws RemoteException {
        int cr = getCredits();
        if (((System.nanoTime() - this.lastBoost) > this.speed)) {
            if (super.removeCredits(toLevelUpSpeed)) {
                speed += 1;
                toLevelUpSpeed *= 10;
                return true;
            }
        }
        System.err.println("Need " + toLevelUpSpeed + " credits to level up speed, current credits: " + cr);
        return false;
    }

    /**
     * Restore a block's hitpoints for an amount equal to the players repair rating
     * @param b the block to be repaired
     * @return true if the block was repaired, false if the block was already destoryed
     * @throws RemoteException if rmi fails
     */
    public boolean repair(GameBlock b) throws RemoteException{
        int p = b.repair(getRepairRating());
        if(p >= 0){
            this.gainCredits(p);
            return true;
        }else{
            return false;
        }

    }

    /**
     * Increases the number of available shields if the player has enough credits
     *
     * @return the number of shields available to the player
     * @throws RemoteException
     */
    public int buyShield() throws RemoteException {
        if (super.removeCredits(shieldPrice)) {
            shields++;
        } else {
            System.err.println("Not enough credits to buy a shield, " + shieldPrice + " credits needed");
        }
        return shields;
    }

    /**
     * Temporarily increase the player's speed if he has sufficient credits, and his boost is not in cooldown
     *
     * @return true if the boost succeeded, false otherwise
     * @throws RemoteException
     */
    synchronized public boolean boost() throws RemoteException {
        if (((System.nanoTime() - this.lastBoost) > this.speed)
                && super.removeCredits(100)) {
            this.speed = this.speed / 2;
            return true;
        }
        return false;
    }

    /**
     * Reset the player's speed back to the original pre-boost value
     *
     * @throws RemoteException
     */
    synchronized public void resetBoost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost) > this.speed) {
            this.speed = this.speed * 2;
        }
    }
}

