import javax.net.ssl.SSLContext;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ChunkLightTuna on 4/2/17.
 */
public class LocalState extends UnicastRemoteObject implements RemoteState {
    final String name;

    final int height;
    final int width;
    final int depth;
    private final Cube cube;
    private final long start;
    private final Object playerLock;
    ExecutorService stateExecutor;

    private ConcurrentHashMap<String, Player> players;
    private ConcurrentHashMap<String, Attacker> attackers;
    private ConcurrentHashMap<String, Defender> defenders;
    private ArrayList<Player> leaderboard;

    public LocalState(String name, int size, int blockHp) throws RemoteException {
        synchronized (this) {
            this.name = name;
            this.width = size;
            this.height = size;
            this.depth = size;
            this.stateExecutor = Executors.newFixedThreadPool(16);
            this.cube = new Cube(size, blockHp);

            this.players = new ConcurrentHashMap<>();
            this.attackers = new ConcurrentHashMap<>();
            this.defenders = new ConcurrentHashMap<>();
            this.leaderboard = new ArrayList<>();

            this.playerLock = new Object();
            this.start = System.nanoTime();
        }
    }

    public RemotePlayer login(String username) throws RemoteException {
        synchronized (playerLock) {
            try {
                if (players.get(username).login()) {
                    System.err.println("Success");
                    System.err.println(players.get(username).unameToString());
                    return players.get(username);
                }
            } catch (Exception e) {
                return null;
            }
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

    public synchronized boolean register(String username, int role) throws RemoteException {
        synchronized (playerLock) {
            if (players.get(username) == null) {
                if (role == 1) {
                    System.err.println("Registered attacker " + username);
                    Attacker atk = new Attacker(username, 0, 0);
                    players.putIfAbsent(username, atk);
                    attackers.putIfAbsent(username, atk);
                    System.err.println(attackers.get(username).toString());
                } else {
                    System.err.println("Registered defender " + username);
                    Defender def = new Defender(username, 0, 0);
                    players.putIfAbsent(username, def);
                    defenders.putIfAbsent(username, def);
                }
                leaderboard.add(players.get(username));
                return true;
            }
            return false;
        }
    }

    public String printLeaderBoards() throws RemoteException {
        Collections.sort(leaderboard);
        int min = leaderboard.size() >= 10 ? 10 : leaderboard.size();
        String s = "";
        for (int i = 0; i < min; i++) {
            s += "1. " + leaderboard.get(i).unameToString() + " " + leaderboard.get(i).getScore() + "\n";
        }
        return s;
    }

    public int printStatus() throws RemoteException {
        if (!this.cube.isAlive()) {
            //Attackers won
            System.err.println("Cube destroyed, attackers won!");
            return -1;
        }
        if (((System.nanoTime() - start) / 1e9) > 600) {
            System.err.println("Cube is not destoryed, defenders won");
            return 1;
        }
        System.err.println(this.cube.isAlive());
        return 0;
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

    public int requestPrimary(String user, int role, String block) throws RemoteException {
        int result;
        if (role == 1) {
            try {
                result = attackers.get(user).attack(cube.getBlock(block));
                if (cube.getBlock(block).getHp() <= 0) {
                    System.err.println("Removing "+block.toString());
                    int pos = cube.currentLayer.layer.indexOf(cube.getBlock(block));
                    System.err.println(pos);
                    System.err.println(cube.currentLayer.layer.remove(pos));
                }
            } catch (Exception e) {
                result = -1;
            }
        } else {
            try {
                result = defenders.get(user).repair(cube.getBlock(block));
            } catch (Exception e) {
                result = -1;
            }
        }
        return result;
    }

    @Override
    public int requestSecondary(String user, int role, String block) throws RemoteException {
        if (role == 1) {
            String tokens[] = block.split("_");
            int number = Integer.parseInt(tokens[tokens.length - 1]);
            int n1 = number - 2;
            int n2 = number - 1;
            int n3 = number + 1;
            int n4 = number + 2;
            GameBlock b1 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n1);
            GameBlock b2 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n2);
            GameBlock b3 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n3);
            GameBlock b4 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n4);
            int r1 = attackers.get(user).attack(cube.getBlock(block));
            int r2 = attackers.get(user).attack(b1);
            int r3 = attackers.get(user).attack(b2);
            int r4 = attackers.get(user).attack(b3);
            int r5 = attackers.get(user).attack(b4);
            return r1 + r2 + r3 + r4 + r5;
        } else {
            return defenders.get(user).shield(cube.getBlock(block));
        }
    }

    @Override
    public int requestBoost(String user, int role) throws RemoteException {
        int res;
        try {
            if (role == 1) {
                res = attackers.get(user).boost();
            } else {
                res = defenders.get(user).boost();
            }
            return res;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int levelPrimary(String user, int role) throws RemoteException {
        try {
            if (role == 1) {
                return attackers.get(user).levelUpAr();
            } else {
                return defenders.get(user).levelUpRr();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int levelSecondary(String user, int role) throws RemoteException {
        try {
            if (role == 1) {
                return attackers.get(user).levelUpSpeed();
            } else {
                return defenders.get(user).levelUpSpeed();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int buy(String user, int role) throws RemoteException {
        try {
            if (role == 1) {
                return attackers.get(user).buyBomb();
            } else {
                return defenders.get(user).buyShield();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String getTargets() {
        return cube.currentLayer.toString();
    }

    @Override
    public String printPlayer(String player, int r) throws RemoteException {
        if (r == 1) {
            return attackers.get(player).print();
        } else {
            return defenders.get(player).print();
        }
    }

}
