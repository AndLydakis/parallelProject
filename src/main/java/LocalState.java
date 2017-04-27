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

    public int findRole(String username) {
        int role;
        if (attackers.containsKey(username)) {
            role = 1;
        } else if (defenders.containsKey(username)) {
            role = 0;
        } else {
            role = -1;
        }
        return role;
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
            return 1;
        }
        if (((System.nanoTime() - start) / 1e9) > 600) {
            System.err.println("Cube is not destoryed, defenders won");
            return -1;
        }
        System.err.println(this.cube.isAlive());
        return 0;
    }

    public String parseRequest(String request) throws RemoteException {
        String[] tokens = request.split("-");
        String action = tokens[0];
        System.err.println("Action : " + action);
        String resp = "";
        int res;
        switch (action) {
            case "REGISTER": {
                if (register(tokens[1], Integer.parseInt(tokens[2]))) {
                    res = 1;
                } else {
                    res = 0;
                }
                resp = "REGISTER-" + res + "-" + tokens[1] + "-" + tokens[2];
                break;
            }
            case "LOGIN": {
                if (login(tokens[1]) != null) {
                    res = 1;
                } else {
                    res = 0;
                }
                resp = "LOGIN-" + res + "-" + tokens[1] + "-" + findRole(tokens[1]);
                break;
            }
            case "LOGOUT": {
                if (logout(tokens[1])) {
                    res = 1;
                } else {
                    res = 0;
                }
                resp = "LOGOUT-" + res + "-" + tokens[1];
                break;
            }
            case "ATTACK": {
                res = requestPrimary(tokens[1], 1, tokens[2]);
                resp = "ATTACK-(" + res + ")-" + tokens[1];
                break;
            }
            case "REPAIR": {
                res = requestPrimary(tokens[1], 0, tokens[2]);
                resp = "REPAIR-(" + res + ")-" + tokens[1];
                break;
            }
            case "BOMB": {
                res = requestPrimary(tokens[1], 0, tokens[2]);
                resp = "BOMB-(" + res + ")-" + tokens[1];
                break;
            }
            case "SHIELD": {
                res = requestPrimary(tokens[1], 0, tokens[2]);
                resp = "SHIELD-(" + res + ")-" + tokens[1];
                break;
            }
            case "BUYBOMB": {
                res = buy(tokens[1], 1);
                resp = "BUYBOMB-(" + res + ")-" + tokens[1];
                break;
            }
            case "BUYSHIELD": {
                res = buy(tokens[1], 0);
                resp = "BUYBOMB-(" + res + ")-" + tokens[1];
                break;
            }
            case "LVLATK": {
                res = levelPrimary(tokens[1], 0);
                resp = "LVLATK-(" + res + ")-" + tokens[1];
                break;
            }
            case "LVLREP": {
                res = levelPrimary(tokens[1], 0);
                resp = "LVLREP-(" + res + ")-" + tokens[1];
                break;
            }
            case "LVLSPD": {
                res = levelSecondary(tokens[1], 0);
                resp = "LVLATK-(" + res + ")-" + tokens[1];
                break;
            }
            case "GETTARGETS": {
                resp = "TARGETS-" + getTargets() + "\r\n";
                break;
            }
            case "BOOST": {
                res = requestBoost(tokens[1], Integer.parseInt(tokens[2]));
                resp = "BOOST-(" + res + ")\r\n";
                break;
            }
            case "GETPLAYER": {
                int role = Integer.parseInt(tokens[2]);
                String pl;
                if (role == 1) {
                    pl = attackers.get(tokens[1]).print();
                } else {
                    pl = defenders.get(tokens[1]).print();
                }
                resp = "GETPLAYER-" + pl + "\r\n";
                break;
            }
        }
        System.err.println(resp);
        return resp;
    }

    public int requestPrimary(String user, int role, String block) throws RemoteException {
        int result;
        if (role == 1) {
            try {
                result = attackers.get(user).attack(cube.getBlock(block));
                if (cube.getBlock(block).getHp() <= 0) {
                    System.err.println("Removing " + cube.getBlock(block).toString());
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
        try {
            if (role == 1) {
                String tokens[] = block.split("_");
                ArrayList<GameBlock> targets = new ArrayList<>();
                int number = Integer.parseInt(tokens[tokens.length - 1]);
                int n1 = number - 2;
                int n2 = number - 1;
                int n3 = number + 1;
                int n4 = number + 2;
                int r1;
                GameBlock b1 = cube.getBlock(block);
                GameBlock b2 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n1);
                GameBlock b3 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n2);
                GameBlock b4 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n3);
                GameBlock b5 = cube.getBlock(tokens[0] + "_" + tokens[1] + "_" + n4);
                if (b1 != null) {
                    targets.add(b1);
                }
                if (b2 != null) {
                    targets.add(b1);
                }
                if (b3 != null) {
                    targets.add(b1);
                }
                if (b4 != null) {
                    targets.add(b1);
                }
                if (b5 != null) {
                    targets.add(b1);
                }
                r1 = attackers.get(user).bomb(targets);
                return r1;
            } else {
                /*
                positive : shielding succeded
                negative : already shielded/ no shields available
                0: error
                 */
                return defenders.get(user).shield(cube.getBlock(block));
            }
        } catch (Exception e) {
            return -1;
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
