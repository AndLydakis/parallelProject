import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ChunkLightTuna on 4/2/17.
 */
public class LocalState extends UnicastRemoteObject implements RemoteState {
    final String name;

    final int height;
    final int width;
    final int depth;
    private final Cube cube;

    private ConcurrentHashMap<String, Player> players;
    private ArrayList<Player> attackers;
    private ArrayList<Player> defenders;

    public synchronized Player   login(String username) throws RemoteException {
        if (players.get(username) != null) {
            players.get(username).login();
            return players.get(username);
        }
        return null;
    }

    public synchronized boolean logout(String username) throws RemoteException {
        if (players.get(username) != null) {
            players.get(username).logout();
            return true;
        }
        return false;
    }

    public synchronized boolean register(String username, int role) {
        if (players.get(username) == null) {

            return true;
        }
        return false;
    }

    public String parseRequest(String req) throws RemoteException {
        String[] tokens = req.split(" ");
        String operation = tokens[0];
        String username = tokens[1];
        switch (operation) {
            case "ATTACK": {
                Attacker atk = (Attacker) players.get(username);
                GameBlock gb = cube.getBlock(tokens[2]);
                atk.gainCredits(gb.attack(atk.getAttackRating()));
                return atk.toString();
            }
            case "REPAIR": {
                Defender def = (Defender) players.get(username);
                GameBlock gb = cube.getBlock(tokens[2]);
                def.gainCredits(gb.repair(def.getRepairRating()));
                return def.toString();
            }
            case "SHIELD":
                Defender def = (Defender) players.get(username);
                GameBlock gb = cube.getBlock(tokens[2]);
                gb.shield(def, def.getRepairRating());
                break;
            case "REGISTER":

                break;
            case "LOGIN":
                break;
            case "LOGOUT":
                break;
            case "LVL_REP":
                break;
            case "LVL_AR":
                break;
            case "LVL_SP":
                break;
            case "BOOST":
                break;
            case "BB":
                break;
            case "BS":
                break;
            case "LDB":
                break;
            default:
                break;
        }
        return "";
    }


    public LocalState(String name, int size, int blockHp) throws RemoteException {
        synchronized (this) {
            this.name = name;
            this.width = size;
            this.height = size;
            this.depth = size;

            cube = new Cube(size, blockHp);

            players = new ConcurrentHashMap<>();
            attackers = new ArrayList<>();
            defenders = new ArrayList<>();
        }
    }

    void getFace(int side) {
        if (side > 5) {
            throw new IllegalArgumentException("a cube can only have 6 sides");
        }

        int[] slice = new int[depth];
//            for (int[][] ints : cube) {
//                for (int[] anInt : ints) {
//                    anInt[width-1]
//                }
//            }


    }
}