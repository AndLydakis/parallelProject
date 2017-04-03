import java.io.Serializable;

/**
 * Created by lydakis-local on 4/3/17.
 */
public abstract class Operation implements Serializable{
    static final long serialVersionUID = 876987698768012L;

    Operation(){}

    public static Operation attack(Player p, GameBlock b){
        return new Operation() {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    public static Operation repair(Player p, GameBlock b){
        return new Operation() {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    public static Operation bomb(Player p, GameBlock b){
        return new Operation() {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    public static Operation shield(Player p, GameBlock b){
        return new Operation() {
            @Override
            public String toString() {
                return p.toString()+" SHIELDS "+b.toString();
            }
        };
    }

}
