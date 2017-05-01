import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by lydakis-local on 4/2/17.
 * Attackers are tasked with destroying the game blocks
 * the can attack a single block, or bomb nearby blocks
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

    /**
     * Constructor
     *
     * @param un String username
     * @param s  int score
     * @param cr int credits
     * @throws RemoteException when RMI bad things happen
     */
    Attacker(String un, int s, int cr) throws RemoteException {
        super(un, 1, s, cr);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBoost = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
    }

    /**
     * Constructor
     *
     * @param un      String username
     * @param score   int score
     * @param credits int credits
     * @param attack  int attack rating
     * @param speed   int speed
     * @param bombs   int bombs
     * @throws RemoteException when RMI bad thing happen
     */
    Attacker(String un, int score, int credits, int attack, int speed, int bombs) throws RemoteException {
        super(un, 1, score, credits);
        this.attackRating = attack;
        this.speed = speed;
        this.bombs = bombs;
        this.lastAttack = -10000L;
        this.lastBoost = -10000L;
        this.lastBomb = -10000L;
    }

    /**
     * Constructor from String
     *
     * @param s the string to create the player from
     * @throws RemoteException when RMI bad things happen
     */
    public Attacker(String s) throws RemoteException {
        super(s);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
    }

    /**
     * Set the attack rating of the player
     *
     * @param a attack rating
     */
    void setAttackRating(int a) {
        this.attackRating = a;
    }

    /**
     * Set the number of bombs of the player
     *
     * @param a the number of bombs
     */
    void setBombs(int a) {
        this.bombs = a;
    }

    /**
     * Set the speed of the player
     *
     * @param a the speed to set
     */
    void setSpd(int a) {
        this.speed = a;
    }

    /**
     * Set the credits needed to level attack rating
     *
     * @param a the credits needed to level attack rating
     */
    void setLevelAr(int a) {
        this.toLevelUpAr = a;
    }

    /**
     * Set the credits needed to level speed
     *
     * @param a the credits needed to level speed
     */
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
     * @return the speed of the player
     * @throws RemoteException when RMI bad thing happen
     */
    public double getSpeed() throws RemoteException {
        return this.speed;
    }

    /**
     * prints the attacker as a string
     *
     * @return the attacker as a string
     * @throws RemoteException when RMI bad things happen
     */
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
     * @throws RemoteException when RMI bad thing happen
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
     * @throws RemoteException when RMI bad thing happen
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

    /**
     * attacks a central block for 5 times the attack rating and up to 4 other
     * blocks for 2 times the attack rating, and increases the player credits
     * according to the damage dealt
     *
     * @param blocks an array of blocks to be attacked
     * @return the total damage dealt to all the blocks
     * @throws RemoteException when RMI bad things happen
     */
    int bomb(ArrayList<GameBlock> blocks) throws RemoteException {
//        System.err.println("Bombing " + blocks.get(0).toString());
        if (!canAttack()) return 0;
        int sum = 0;
        if (blocks.size() == 0) return sum;
        int res = 0;
        if (bombs > 0) {
            try {
                sum = blocks.get(0).attack(getAttackRating() * 5);
            } catch (Exception e) {
                res++;
            }
            for (int i = 1; i < blocks.size(); i++) {
                try {
                    sum += blocks.get(i).attack(getAttackRating() * 2);
                } catch (Exception e) {
                    res++;
                }
            }
            lastAttack = System.nanoTime();
        }
        if (res != blocks.size()) bombs--;
        if (sum > 0) gainCredits(sum);
        return sum;
    }

    /**
     * Increases the number of available bombs if the player has enough credits
     *
     * @return the number of bombs available to the player
     * @throws RemoteException when RMI bad thing happen
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
     * @throws RemoteException when RMI bad thing happen
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
     * @throws RemoteException when RMI bad thing happen
     */
    private synchronized void resetBoost() throws RemoteException {
        if ((System.nanoTime() - this.lastBoost) / 1e9 > boostCooldown * 10) {
            boosted = false;
//            System.err.println("Boost reset");
        }
    }
}
