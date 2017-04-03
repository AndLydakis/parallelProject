import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ChunkLightTuna on 4/2/17.
 */
public class LocalState extends UnicastRemoteObject implements State {
    final String name;
    //TODO DO WE NEED BOTH A STATE AND A BOARD ?

    final int height;
    final int width;
    final int depth;
    private final Cube cube;

    private ConcurrentHashMap<String, Player> players;
    private ArrayList<Player> attackers;
    private ArrayList<Player> defenders;

    public synchronized Player login(String username) throws RemoteException {
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

    public LocalState(String name, int width, int height, int depth, int blockHp) throws RemoteException {
        synchronized (this) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.depth = depth;

            cube = new Cube(width, height, depth, blockHp);

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
