/**
 * Created by lydakis-local on 4/2/17.
 */
public class GameBlock {
    private int x;
    private int y;
    private int z;
    private int hp;
    private int shielded;
    private Player shielder;

    public GameBlock(int x, int y, int z, int hp){
        this.x = x;
        this.y = y;
        this.z = z;
        this.hp = hp;
        shielded = -1;
        shielder = null;
    }

    public int isShielded(){
        return this.shielded;
    }

    public int getHp(){
        return this.hp;
    }
}
