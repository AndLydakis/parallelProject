import java.util.ArrayList;
import java.util.Arrays;
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

    Cube(int width, int height, int depth, int blockHp) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        cubeMap = new ConcurrentHashMap<>();
        cube = new GameBlock[width][height][depth];

        for(int w = 0; w < this.width; w++){
            for(int h = 0; h < this.height; h++){
                for(int d = 0; d < this.depth; d++) {
                    cube[w][h][d] = new GameBlock(w, h, d, blockHp);
                    cubeMap.put(coordToString(w,h,d), cube[w][h][d]);
                }
            }
        }
    }

    private String coordToString(int w, int h, int d){
        return(w+"_"+h+"_"+d);
    }

    public ArrayList<Cube> returnFace(){
        return new ArrayList<>();
    }
}
