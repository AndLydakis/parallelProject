import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Creates a cube comprised of different layers
 * that include a decreasing amount of blocks
 */
public class Cube implements Serializable {
    final ConcurrentHashMap<String, GameBlock> cubeMap;
    private final ConcurrentLinkedQueue<Layer> layers;
    Layer currentLayer;
    private ConcurrentHashMap<String, GameBlock> activeCubes;

    /**
     * Layers contain different faces
     * At the moment the whole layer is returned to
     * querying players
     */
    class Layer implements Serializable {
        ArrayList<ArrayList<GameBlock>> faces;
        ArrayList<GameBlock> face1;
        ArrayList<GameBlock> face2;
        ArrayList<GameBlock> face3;
        ArrayList<GameBlock> face4;
        ArrayList<GameBlock> face5;
        ArrayList<GameBlock> face6;
        ArrayList<GameBlock> layer;

        /**
         * Creates a layer based on the given parameters
         *
         * @param level   the depth of the level in the cube
         * @param size    the dimension of each face
         * @param blockHp the hitpoints of each block
         */
        Layer(int level, int size, int blockHp) {
            System.err.println("Layer #" + level + ",  size " + size);
            face1 = new ArrayList<>();
            face2 = new ArrayList<>();
            face3 = new ArrayList<>();
            face4 = new ArrayList<>();
            face5 = new ArrayList<>();
            face6 = new ArrayList<>();
            faces = new ArrayList<>();
            layer = new ArrayList<>();

            ArrayList<GameBlock> edge1 = new ArrayList<>();
            ArrayList<GameBlock> edge2 = new ArrayList<>();
            ArrayList<GameBlock> edge3 = new ArrayList<>();
            ArrayList<GameBlock> edge4 = new ArrayList<>();
            ArrayList<GameBlock> edge5 = new ArrayList<>();
            ArrayList<GameBlock> edge6 = new ArrayList<>();
            ArrayList<GameBlock> edge7 = new ArrayList<>();
            ArrayList<GameBlock> edge8 = new ArrayList<>();
            ArrayList<GameBlock> edge9 = new ArrayList<>();
            ArrayList<GameBlock> edge10 = new ArrayList<>();
            ArrayList<GameBlock> edge11 = new ArrayList<>();
            ArrayList<GameBlock> edge12 = new ArrayList<>();

            GameBlock corner1;
            GameBlock corner2;
            GameBlock corner3;
            GameBlock corner4;
            GameBlock corner5;
            GameBlock corner6;
            GameBlock corner7;
            GameBlock corner8;

            if (size == 1) {
                GameBlock block = new GameBlock(level, 1, 1, blockHp);
                face1.add(block);
                face2.add(block);
                face3.add(block);
                face4.add(block);
                face5.add(block);
                face6.add(block);
                layer.add(block);
                System.err.println("1 " + layer.size());
            } else {
                int faceSize = size - 2;
                int idx = 0;
                for (int i = 0; i < faceSize * faceSize; i++) {
                    face1.add(new GameBlock(level, 1, idx++, blockHp));
                    face2.add(new GameBlock(level, 2, idx++, blockHp));
                    face3.add(new GameBlock(level, 3, idx++, blockHp));
                    face4.add(new GameBlock(level, 4, idx++, blockHp));
                    face5.add(new GameBlock(level, 5, idx++, blockHp));
                    face6.add(new GameBlock(level, 6, idx++, blockHp));
                }

                for (int i = 0; i < faceSize * faceSize; i++) {
                    layer.add(face1.get(i));
                    layer.add(face2.get(i));
                    layer.add(face3.get(i));
                    layer.add(face4.get(i));
                    layer.add(face5.get(i));
                    layer.add(face6.get(i));
                }

                System.err.println("1 " + layer.size());

                for (int i = 0; i < faceSize; i++) {
                    edge1.add(new GameBlock(level, 2, idx++, blockHp));
                    edge2.add(new GameBlock(level, 1, idx++, blockHp));
                    edge3.add(new GameBlock(level, 3, idx++, blockHp));
                    edge4.add(new GameBlock(level, 3, idx++, blockHp));
                    edge5.add(new GameBlock(level, 5, idx++, blockHp));
                    edge6.add(new GameBlock(level, 6, idx++, blockHp));
                    edge7.add(new GameBlock(level, 4, idx++, blockHp));
                    edge8.add(new GameBlock(level, 4, idx++, blockHp));
                    edge9.add(new GameBlock(level, 4, idx++, blockHp));
                    edge10.add(new GameBlock(level, 2, idx++, blockHp));
                    edge11.add(new GameBlock(level, 2, idx++, blockHp));
                    edge12.add(new GameBlock(level, 1, idx++, blockHp));
                }

                for (int i = 0; i < edge1.size(); i++) {
                    layer.add(edge1.get(i));
                    layer.add(edge2.get(i));
                    layer.add(edge3.get(i));
                    layer.add(edge4.get(i));
                    layer.add(edge5.get(i));
                    layer.add(edge6.get(i));
                    layer.add(edge7.get(i));
                    layer.add(edge8.get(i));
                    layer.add(edge9.get(i));
                    layer.add(edge10.get(i));
                    layer.add(edge11.get(i));
                    layer.add(edge12.get(i));
                }
                System.err.println("2 " + layer.size());

                for (int i = 0; i < faceSize; i++) {
                    face1.add(edge2.get(i));
                    face1.add(edge6.get(i));
                    face1.add(edge8.get(i));
                    face1.add(edge12.get(i));

                    face2.add(edge1.get(i));
                    face2.add(edge10.get(i));
                    face2.add(edge11.get(i));
                    face2.add(edge12.get(i));

                    face3.add(edge1.get(i));
                    face3.add(edge2.get(i));
                    face3.add(edge3.get(i));
                    face3.add(edge4.get(i));

                    face4.add(edge3.get(i));
                    face4.add(edge7.get(i));
                    face4.add(edge8.get(i));
                    face4.add(edge9.get(i));

                    face5.add(edge4.get(i));
                    face5.add(edge5.get(i));
                    face5.add(edge7.get(i));
                    face5.add(edge10.get(i));

                    face6.add(edge5.get(i));
                    face6.add(edge6.get(i));
                    face6.add(edge9.get(i));
                    face6.add(edge11.get(i));
                }

                corner1 = new GameBlock(level, 1, idx++, blockHp);
                face1.add(corner1);
                face2.add(corner1);
                face3.add(corner1);
                corner2 = new GameBlock(level, 1, idx++, blockHp);
                face1.add(corner2);
                face3.add(corner2);
                face4.add(corner2);
                corner3 = new GameBlock(level, 2, idx++, blockHp);
                face2.add(corner3);
                face3.add(corner3);
                face5.add(corner3);
                corner4 = new GameBlock(level, 3, idx++, blockHp);
                face3.add(corner4);
                face4.add(corner4);
                face5.add(corner4);
                corner5 = new GameBlock(level, 1, idx++, blockHp);
                face1.add(corner5);
                face2.add(corner5);
                face6.add(corner5);
                corner6 = new GameBlock(level, 4, idx++, blockHp);
                face4.add(corner6);
                face5.add(corner6);
                face6.add(corner6);
                corner7 = new GameBlock(level, 1, idx++, blockHp);
                face1.add(corner7);
                face4.add(corner7);
                face6.add(corner7);
                corner8 = new GameBlock(level, 2, idx, blockHp);
                face2.add(corner8);
                face5.add(corner8);
                face6.add(corner8);

                layer.add(corner1);
                layer.add(corner2);
                layer.add(corner3);
                layer.add(corner4);
                layer.add(corner5);
                layer.add(corner6);
                layer.add(corner7);
                layer.add(corner8);
                System.err.println("3 " + layer.size());
            }

        }

        /**
         * @return true if there are hitpoints remaining in
         * the blocks in the layer, false otherwise
         * @throws RemoteException if rmi fails
         */
        synchronized boolean isAlive() throws RemoteException {
            if (layer == null) return false;
            if (layer.size() == 0) return false;

            try {
                for (GameBlock gb : layer) {
                    if (gb != null && gb.getHp() > 0) {
                        return true;
                    }
                }
            } catch (NoSuchElementException e) {
                //maybe it died while we were checking?
            }
            return false;
        }

        /**
         * print out the blocks in the layer
         *
         * @return every block in the layer as one string
         */
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (GameBlock b : layer) {
                s.append(b.toString()).append("\n");
            }
            return s.toString();
        }

        String toStringHp() {
            StringBuilder s = new StringBuilder();
            for (GameBlock b : layer) {
                s.append(b.toStringHp()).append("\n");
            }
            return s.toString();
        }
    }

    Cube(int size, int blockHp) {
        size = (size % 2 == 0) ? (size - 1) : size;
        cubeMap = new ConcurrentHashMap<>();
        activeCubes = new ConcurrentHashMap<>();
        layers = new ConcurrentLinkedQueue<>();
        int level = 1;
        for (int i = size; i > 0; i -= 2) {
            System.err.println("Adding layer " + i);
            layers.add(new Layer(level, i, blockHp));
            level++;
        }

        for (Layer layer : layers) {
            for (GameBlock block : layer.layer) {
                cubeMap.putIfAbsent(block.toString(), block);
            }
        }

        currentLayer = layers.poll();
        for (GameBlock gb : currentLayer.layer) {
            System.err.println(gb.toString());
        }
        System.err.println("--------------");
        for (Layer l : layers) {
            for (GameBlock gb : l.layer) {
                System.err.println(gb.toString());
            }
            System.err.println("--------------");
        }
    }

    private String coordToString(int w, int h, int d) {
        return (w + "_" + h + "_" + d);
    }

    synchronized boolean isAlive() throws RemoteException {
        if (currentLayer == null) {
            currentLayer = layers.poll();
            if (currentLayer == null) {
                return false;
            }
        }
        if (currentLayer.isAlive()) {
            return true;
        } else {
            System.err.println("Layer Destoryed");
            currentLayer = layers.poll();
            return currentLayer != null;
        }
    }

    private int[] stringToCoord(String block) {
        String[] tokens = block.split("_");
        return new int[]{Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])};
    }

    public void checkBlock(String s) throws RemoteException {
        GameBlock bb = activeCubes.get(s);
        if (bb != null) {
            if (bb.getHp() <= 0) {
                activeCubes.remove(s);
            }
        }
    }

    GameBlock getBlock(String s) {
        return cubeMap.get(s);
    }

    public GameBlock getBlock(int w, int h, int d) {
        return cubeMap.get(coordToString(w, h, d));
    }

    public ArrayList<GameBlock> returnFace() {
        return currentLayer.layer;
    }

    public static void main(String[] args) {
        Cube cube = new Cube(3, 1);
        System.err.println(cube.cubeMap.size());
    }
}
