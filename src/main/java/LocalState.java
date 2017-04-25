import javax.net.ssl.SSLContext;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
    ExecutorService stateExecutor;

    private ConcurrentHashMap<String, Player> players;
    private ConcurrentHashMap<String, Attacker> attackers;
    private ConcurrentHashMap<String, Defender> defenders;

    public RemotePlayer login(String username) throws RemoteException {
        if (players.get(username) != null) {
            players.get(username).login();
            System.err.println("Success");
            System.err.println(players.get(username).unameToString());
            return players.get(username);
        }
        System.err.println("Failed");
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
        if (players.get(username) == null) {
            if(role==1){
                System.err.println("Registered attacker "+username);
                Attacker atk = new Attacker(username, 0, 0);
                players.putIfAbsent(username, atk);
                attackers.putIfAbsent(username, atk);
                System.err.println(attackers.get(username).toString());
            }else{
                System.err.println("Registered defender "+username);
                Defender def = new Defender(username, 0, 0);
                players.putIfAbsent(username, def);
                defenders.putIfAbsent(username, def);
            }
            return true;
        }
        return false;
    }

    public void printStatus() throws RemoteException {
        System.err.println(this.cube.isAlive());
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
            this.stateExecutor = Executors.newFixedThreadPool(16);

            cube = new Cube(size, blockHp);

            players = new ConcurrentHashMap<>();
            attackers = new ConcurrentHashMap<>();
            defenders = new ConcurrentHashMap<>();
        }
    }

    public boolean requestPrimary(String user, int role, String block) throws RemoteException{
        boolean result;
        if(role==1){
            System.err.println("**" + user);
            System.err.println("Attaker "+attackers.get(user).unameToString());
            try {
                result = attackers.get(user).attack(cube.getBlock(block));
                System.err.println("***" + cube.getBlock(block).getHp());
                if(cube.getBlock(block).getHp()<=0){
                    System.err.println(cube.currentLayer.layer.remove(block));
                }
            }catch (Exception e){
                result = false;
            }
        }else{
            try {
                result = defenders.get(user).repair(cube.getBlock(block));
            }catch (Exception e){
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean requestSecondary(String user, int role, String block) throws RemoteException {
        boolean result;
        if(role==1){
            String tokens[] = block.split("_");
            int number = Integer.parseInt(tokens[tokens.length-1]);
            int n1 = number - 2;
            int n2 = number - 1;
            int n3 = number + 1;
            int n4 = number + 2;
            GameBlock b1 = cube.getBlock(tokens[0]+"_"+tokens[1]+"_"+n1);
            GameBlock b2 = cube.getBlock(tokens[0]+"_"+tokens[1]+"_"+n2);
            GameBlock b3 = cube.getBlock(tokens[0]+"_"+tokens[1]+"_"+n3);
            GameBlock b4 = cube.getBlock(tokens[0]+"_"+tokens[1]+"_"+n4);
            boolean r1 = attackers.get(user).attack(cube.getBlock(block));
            boolean r2 = attackers.get(user).attack(b1);
            boolean r3 = attackers.get(user).attack(b2);
            boolean r4 = attackers.get(user).attack(b3);
            boolean r5= attackers.get(user).attack(b4);
            return r1||r2||r3||r4||r5;
        }else{
            result = defenders.get(user).shield(cube.getBlock(block));
        }
        return result;
    }

    @Override
    public boolean requestBoost(String user) throws RemoteException {
        return false;
    }

    @Override
    public boolean levelPrimary(String user, int role) throws RemoteException {
        if(role == 1){
            return attackers.get(user).levelUpAr();
        }else{
            return defenders.get(user).levelUpRr();
        }
    }

    @Override
    public boolean levelSecondary(String user, int role) throws RemoteException {
        if(role == 1){
            return attackers.get(user).levelUpSpeed();
        }else{
            return defenders.get(user).levelUpSpeed();
        }
    }

    @Override
    public boolean buy(String user, int role) throws RemoteException {
        if(role == 1){
            return attackers.get(user).levelUpSpeed();
        }else{
            return defenders.get(user).levelUpSpeed();
        }
    }

    @Override
    public String getTargets() {
        return cube.currentLayer.toString();
    }

    @Override
    public String printPlayer(String player, int r) throws RemoteException {
        if(r == 1){
            return attackers.get(player).print();
        }else{
            return defenders.get(player).print();
        }
    }

}
