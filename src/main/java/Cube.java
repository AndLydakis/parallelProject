import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lydakis-local on 4/2/17.
 */
public class Cube {
    final int height;
    final int width;
    final int depth;
    //Do we want an array or a concurrent hashmap ?
    final GameBlock[][][] cube;
    final ConcurrentHashMap<String, GameBlock> cubeMap;
    ConcurrentHashMap<String, GameBlock> activeCubes;

    Cube(int width, int height, int depth, int blockHp) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        cubeMap = new ConcurrentHashMap<>();
        activeCubes = new ConcurrentHashMap<>();
        cube = new GameBlock[width][height][depth];

        for (int w = 0; w < this.width; w++) {
            for (int h = 0; h < this.height; h++) {
                for (int d = 0; d < this.depth; d++) {
                    cube[w][h][d] = new GameBlock(w, h, d, blockHp);
                    cubeMap.put(coordToString(w, h, d), cube[w][h][d]);
                }
            }
        }
    }

    private String coordToString(int w, int h, int d) {
        return (w + "_" + h + "_" + d);
    }

    private int[] stringToCoord(String block) {
        String[] tokens = block.split("_");
        return new int[]{Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])};
    }

    public void checkBlock(String s) throws RemoteException {
        GameBlock bb = activeCubes.get(s);
        if(bb!=null) {
            if(bb.getHp()<=0){
                activeCubes.remove(s);
            }
        }
    }

    public GameBlock getBlock(String s) {
        return cubeMap.get(s);
    }

    public GameBlock getBlock(int w, int h, int d) {
        return cubeMap.get(coordToString(w, h, d));
    }

    public ArrayList<Cube> returnFace() {
        return new ArrayList<>();
    }
}
