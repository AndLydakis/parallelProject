import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class Server {
    private ConcurrentHashMap<String, Player> players;
    private ArrayList<Player> attackers;
    private ArrayList<Player> defenders;

    synchronized private Player login(String username){
       if(players.get(username)!=null){
           players.get(username).login();
           return players.get(username);
       }
       return null;
    }

    synchronized private void logout(String username){
        if(players.get(username)!=null){
            players.get(username).logout();
        }
    }
}
