/**
 * Created by lydakis-local on 4/2/17.
 */
public class Player {
    final String userName;
    private int score;
    private int credits;
    private int level;

    private final double boostCooldown = 10.0;

    public Player(String un, int s, int cr){
        this.userName = un;
        this.score = s;
        this.credits = cr;
        this.level = 1;
    }

    public int getLevel(){
        return this.level;
    }

    public int getCredits(){
        return this.credits;
    }

    public int getScore(){
        return this.score;
    }

    public double getBoostCooldown(){
        return this.boostCooldown;
    }

}
