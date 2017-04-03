/**
 * Created by lydakis-local on 4/3/17.
 *
 * Records of player actions
 */
public class Record implements java.io.Serializable {
    private static final long serialVersionUID = 5634576354735465633L;

    static final String ATTACK_RECORD_TYPE = "%s ATTACKS %s";
    static final String REPAIR_RECORD_TYPE = "%s REPAIRS %s";
    static final String SHIELDS_RECORD_TYPE = "%s SHIELDS %s";
    static final String GAINS_RECORD_TYPE = "%s GAINS %d CREDITS";
    static final String LOSES_RECORD_TYPE = "%s LOSES %d CREDITS";
    static final String SCORE_RECORD_TYPE = "%s SCORES %d CREDITS";

    public final int credits;
    public final String player;
    public final String block;
    public final String type;

    @Override
    public String toString(){
        return type;
    }

    Record(String type, int credits, String pl, String bl){
        this.type = type;
        this.credits = credits;
        this.player = pl;
        this.block = bl;
    }
}
