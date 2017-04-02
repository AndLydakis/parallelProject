import java.util.Arrays;

/**
 * Created by ChunkLightTuna on 4/2/17.
 */
public class State {


    public static class Board {
        final int height;
        final int width;
        final int depth;
        int[][][] cube;

        Board(int width, int height, int depth, int blockHp) {
            this.width = width;
            this.height = height;
            this.depth = depth;


            cube = new int[width][height][depth];



            for (int[][] ints : cube) {
                for (int[] anInt : ints) {
                    Arrays.fill(anInt, blockHp);
                }
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
}
