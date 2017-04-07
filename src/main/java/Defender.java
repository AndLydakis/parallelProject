import java.rmi.RemoteException;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Defender extends Player{

    int shields;
    double speed;
    int repairRating;
    long lastRepair;
    long lastShield;
    private long lastBoost;
    private int toLevelUpRr = 10;
    private int toLevelUpSpeed = 10;
    private final int shieldPrice = 500;

    public Defender(String un, int s, int cr) throws RemoteException {
        super(un, s, cr);
        this.speed = 1.0;
        this.repairRating = 1;
        this.lastRepair = -1l;
        this.lastShield = -1l;
        this.shields = 0;
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
     * @param s String formatted as "SCORE CREDITS LEVEL SPEED REPAIR_RATING SHIELDS"
     */
    public void update(String s){
        super.update(s);
        String[] tokens = s.split(" ");
        this.speed = Double.parseDouble(tokens[3]);
        this.repairRating = Integer.parseInt(tokens[4]);
        this.shields = Integer.parseInt(tokens[5]);
    }

    public String toString(String s) throws RemoteException {
        return (getCredits()+" "+getScore()+" "+shields+" "+speed+" "+repairRating+" "+" "+ toLevelUpRr +" "+toLevelUpSpeed);
    }

    public double getSpeed(){
        return this.speed;
    }

    public int getRepairRating(){
        return this.repairRating;
    }

    public int getShields(){
        return this.shields;
    }

    public boolean canShield(){
        return ((System.nanoTime() - lastRepair)>this.speed);
    }

    public boolean canAttack(){
        return ((System.nanoTime() - this.lastRepair)>this.speed);
    }

    public boolean canBoost() throws RemoteException {
        return ((System.nanoTime() - this.lastBoost)>getBoostCooldown());
    }

    public void levelUpRr() throws RemoteException {
        int cr = getCredits();
        if(getCredits()<= toLevelUpRr){
            repairRating+=1;
            toLevelUpRr *=10;
            return;
        }
        System.err.println("Need "+ toLevelUpRr +" credits to level up repair rating, current credits: "+cr);
    }

    public void levelUpSpeed() throws RemoteException {
        int cr = getCredits();
        if(getCredits()<=toLevelUpSpeed){
            speed+=1;
            toLevelUpSpeed *=10;
            return;
        }
        System.err.println("Need "+toLevelUpSpeed+" credits to level up speed, current credits: "+cr);
    }

    public void buyShield() throws RemoteException {
        if(super.removeCredits(shieldPrice)){
            shields++;
        }else{
            System.err.println("Not enough credits to buy a shield, "+shieldPrice+" credits needed");
        }
    }
}

