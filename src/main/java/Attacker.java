import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Attacker extends Player implements Serializable {

    private int bombs;
    private int speed;
    private int attackRating;
    private long lastAttack;
    private long lastBomb;
    private long lastBoost;
    private int toLevelUpAr = 1;
    private int toLevelUpSpeed = 1;
    private int bombPrice = 1;
    private int boostCost = 1;
    private int boostCooldown = 1;
    private int baseCooldown = 5;
    private volatile boolean boosted = false;

    Attacker(String un, int s, int cr) throws RemoteException {
        super(un, 1, s, cr);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBoost = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
    }

    Attacker(String un, int score, int credits, int speed, int attack, int bombs) throws RemoteException {
        super(un, 1, score, credits);
        this.speed = speed;
        this.attackRating = attack;
        this.lastAttack = -10000L;
        this.lastBoost = -10000L;
        this.lastBomb = -10000L;
        this.bombs = bombs;
    }

    public Attacker(String s) throws RemoteException {
        super(s);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
    }

    void setAttackRating(int a) {
        this.attackRating = a;
    }

    void setBombs(int a) {
        this.bombs = a;
    }

    void setSpd(int a) {
        this.speed = a;
    }

    void setLevelAr(int a) {
        this.toLevelUpAr = a;
    }

    void setLevelSpd(int a) {
        this.toLevelUpSpeed = a;
    }

    /**
     * Update player from a socket response
     *
     * @param s String formatted as "SCORE CREDITS LEVEL SPEED ATTACK_RATING BOMBS"
     */
    public void update(String s) throws RemoteException {
        super.update(s);
        String[] tokens = s.split(" ");
        this.speed = Integer.parseInt(tokens[3]);
        this.attackRating = Integer.parseInt(tokens[4]);
        this.bombs = Integer.parseInt(tokens[5]);
    }

    /**
     * Return the speed of the player
     *
     * @return the speed of the player
     */
    public double getSpeed() throws RemoteException {
        return this.speed;
    }

    public String print() throws RemoteException {
        return super.print() +
                "Role: Attacker\n" +
                "Speed: " + speed + "\n" +
                "Attack Rating: " + attackRating + "\n" +
                "Bombs Available: " + bombs;
    }

    /**
     * Return the attack rating of the player
     *
     * @return the attack rating of the player
     */
    private int getAttackRating() throws RemoteException {
        return this.attackRating;
    }

    /**
     * Return the number of bombs available to the player
     *
     * @return the number of bombs
     */
    public int getBombs() throws RemoteException {
        return this.bombs;
    }

    /**
     * Check if the player can attack again
     *
     * @return true iof the player can attack, false otherwise
     */
    private boolean canAttack() throws RemoteException {
        resetBoost();
        if (boosted) {
            return (System.nanoTime() - this.lastAttack) / 1e9 + 10 * this.speed > baseCooldown;
        } else {
            return (System.nanoTime() - this.lastAttack) / 1e9 + this.speed > baseCooldown;
        }
    }

    /**
     * Check if the player can use his boost ability again
     *
     * @return true if the player can boost again, false otherwise
     * @throws RemoteException if rmi fails
     */
    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost) > boostCooldown);
    }


    /**
     * Increase the attack rating if the player has enough credits
     *
     * @return true if the player had enough credits to level up repair rating, false otherwise
     * @throws RemoteException
     */
    int levelUpAr() throws RemoteException {
        int cr = getCredits();
        if (super.removeCredits(toLevelUpAr)) {
            attackRating += 1;
            toLevelUpAr *= 2;
            return attackRating;
        }
        System.err.println("Need " + toLevelUpAr + " credits to level up attack rating, current credits: " + cr);
        return -toLevelUpAr;
    }

    /**
     * Increase the speed of the if the player has enough credits
     *
     * @return true if the player had enough credits to level up speed, false otherwise
     * @throws RemoteException
     */
    int levelUpSpeed() throws RemoteException {
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
     * Damages a block's hitpoints for an amount equal to the players attack rating
     *
     * @param b the block to be repaired
     * @return true if the block was repaired, false if the block was already destroyed
     * @throws RemoteException if rmi fails
     */
    int attack(GameBlock b) throws RemoteException {
        if (!canAttack()) return 0;
//        System.err.println(userName + " attacking " + b.toString());
        int p = b.attack(getAttackRating());
        if (p >= 0) {
            this.gainCredits(p);
        }
        lastAttack = System.nanoTime();
        return p;

    }

    int bomb(ArrayList<GameBlock> blocks) throws RemoteException {
//        System.err.println("Bombing " + blocks.get(0).toString());
        if (!canAttack()) return 0;
        int sum = 0;
        if (blocks.size() == 0) return sum;
        if (bombs > 0) {
            bombs--;
            try {
                sum = blocks.get(0).attack(getAttackRating() * 5);
            } catch (Exception e) {

            }
            for (int i = 1; i < blocks.size(); i++) {
                try {
                    sum += blocks.get(i).attack(getAttackRating() * 2);
                } catch (Exception e) {

                }
            }
            lastAttack = System.nanoTime();
        }
        if (sum > 0) gainCredits(sum);
        return sum;
    }

    /**
     * Increases the number of available bombs if the player has enough credits
     *
     * @return the number of bombs available to the player
     * @throws RemoteException
     */
    int buyBomb() throws RemoteException {
        if (super.removeCredits(bombPrice)) {
            bombs++;
        } else {
//            System.err.println("Not enough credits to buy a shield, " + bombPrice + " credits needed");
            return -bombPrice;
        }
        return bombs;
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
    private synchronized void resetBoost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost) / 1e9 > boostCooldown * 10) {
            boosted = false;
//            System.err.println("Boost reset");
        }
    }
}
