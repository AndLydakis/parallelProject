import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Attacker extends Player {

    private int bombs;
    private int speed;
    private int attackRating;
    private long lastAttack;
    private long lastBomb;
    private long lastBoost;
    private int toLevelUpAr = 10;
    private int toLevelUpSpeed = 10;
    private int bombPrice = 500;

    public Attacker(String un, int s, int cr) throws RemoteException {
        super(un, 1, s, cr);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
    }

    public Attacker(String s) throws RemoteException {
        super(s);
        this.speed = 1;
        this.attackRating = 1;
        this.lastAttack = -10000L;
        this.lastBomb = -10000L;
        this.bombs = 0;
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
                "Speed: " + speed + "\n" +
                "Attack Rating: " + attackRating;
    }

    /**
     * Return the attack rating of the player
     *
     * @return the attack rating of the player
     */
    public int getAttackRating() throws RemoteException {
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
    public boolean canAttack() throws RemoteException {
        return ((System.nanoTime() - this.lastAttack) > this.speed);
    }

    /**
     * Check if the player can use his boost ability again
     *
     * @return true if the player can boost again, false otherwise
     * @throws RemoteException if rmi fails
     */
    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost) > getBoostCooldown());
    }


    /**
     * Increase the attack rating if the player has enough credits
     *
     * @return true if the player had enough credits to level up repair rating, false otherwise
     * @throws RemoteException
     */
    public int levelUpAr() throws RemoteException {
        int cr = getCredits();
        if (super.removeCredits(toLevelUpAr)) {
            attackRating += 1;
            toLevelUpAr *= 10;
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
    public int levelUpSpeed() throws RemoteException {
        int cr = getCredits();
        if (((System.nanoTime() - this.lastBoost) > this.speed)) {
            if (super.removeCredits(toLevelUpSpeed)) {
                speed += 1;
                toLevelUpSpeed *= 10;
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
    public int attack(GameBlock b) throws RemoteException {
        int p = b.attack(getAttackRating());
        if (p >= 0) {
            this.gainCredits(p);
        }
        return p;

    }

    public int bomb(GameBlock[] blocks) throws RemoteException {
        int sum = blocks[0].attack(getAttackRating() * 5);
        for (int i = 1; i < blocks.length; i++) {
            sum += blocks[i].attack(getAttackRating() * 2);
        }
        return sum;
    }

    /**
     * Increases the number of available bombs if the player has enough credits
     *
     * @return the number of bombs available to the player
     * @throws RemoteException
     */
    public int buyBomb() throws RemoteException {
        if (super.removeCredits(bombPrice)) {
            bombs++;
        } else {
            System.err.println("Not enough credits to buy a shield, " + bombPrice + " credits needed");
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
        if (((System.nanoTime() - this.lastBoost) > this.speed)
                && super.removeCredits(100)) {
            this.speed = this.speed / 2;
            return 1;
        }
        return -1;
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
