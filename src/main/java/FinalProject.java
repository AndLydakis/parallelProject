/**
 * Created by ChunkLightTuna on 4/2/17.
 */
public class FinalProject {


    /**
     * @param args doot doot doo
     */
    public static void main(String[] args) {
        System.out.println("dooks");

        LocalState.Board board = new LocalState.Board(20, 20, 20, 10);

        for (int[][] ints : board.cube) {
            for (int[] anInt : ints) {
                for (int i : anInt) {
                    System.out.println(i);
                }
            }
        }
    }
}
