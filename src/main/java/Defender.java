/**
 * Created by lydakis-local on 4/2/17.
 */
public class Defender extends Player{

    int shields;
    double speed;
    double repairRating;
    long lastRepair;
    long lastShield;
    private long lastBoost;
    private int toLevelUpAr = 10;
    private int toLevelUpSpeed = 10;

    public Defender(String un, int s, int cr){
        super(un, s, cr);
        this.speed = 1.0;
        this.repairRating = 1.0;
        this.lastRepair = -1l;
        this.lastShield = -1l;
        this.shields = 0;
    }

    public double getSpeed(){
        return this.speed;
    }

    public double getRepairRating(){
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

    public boolean canBoost(){
        return ((System.nanoTime() - this.lastBoost)>getBoostCooldown());
    }

    public void levelUpRr(){
        int cr = getCredits();
        if(getCredits()<=toLevelUpAr){
            repairRating+=1;
            toLevelUpAr *=10;
            return;
        }
        System.err.println("Need "+toLevelUpAr+" credits to level up repair rating, current credits: "+cr);
    }

    public void levelUpSpeed(){
        int cr = getCredits();
        if(getCredits()<=toLevelUpSpeed){
            speed+=1;
            toLevelUpSpeed *=10;
            return;
        }
        System.err.println("Need "+toLevelUpSpeed+" credits to level up speed, current credits: "+cr);
    }
}

