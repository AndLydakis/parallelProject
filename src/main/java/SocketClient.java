import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by lydakis-local on 4/3/17.
 */
public class SocketClient implements ClientInterface{
    private int role;
    private int lastAction;
    private int lastTarget;
    private Player player;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    List<Integer> attackerOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    List<Integer> defenderOptions = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    public void initialMenu(){
        int s;
        Scanner reader = new Scanner(System.in);
        do {
            System.out.print("\033[H\033[2J");
            System.err.println("Welcome to the bone-zone");
            System.err.println("Would you like to:");
            System.err.println("1. Sign up");
            System.err.println("2. Sign in");
            System.err.println("3. Quit");
            s = reader.nextInt();
        } while (s != 1 && s != 2 && s != 3);
        int ch;
        switch (s) {
            case 1:
                do {
                    try {
                        System.out.print("\033[H\033[2J");
                        System.err.println("Select your role :");
                        System.err.println("1. Attacker");
                        System.err.println("2. Defender");
                        System.err.println("3. Back to menu");
                        ch = reader.nextInt();
                    } catch (Exception e) {
                        System.err.println("Invalid Input");
                        reader.nextLine();
                        ch = -1;
                    }
                } while (ch != 1 && ch != 2 && ch != 3);
                if ((ch == 1) || (ch == 2)) {
                    String user;
                    boolean reg;
                    do {
                        reader.nextLine();
                        System.out.print("\033[H\033[2J");
                        System.err.println("Enter your username (penis jokes will not be tolerated)");
                        System.err.println("Type 'q' to quit");
                        user = reader.nextLine();
                        if (user.equals("q")) {
                            return;
                        }
                        reg = Register(user, ch);
                        if (!reg) user = "";
                    } while (user.equals(""));
                    break;
                }
                break;
            case 2:
                reader.nextLine();
                System.out.print("\033[H\033[2J");
                System.err.println("Welcome back, enter your username");
                String li = reader.nextLine();
                login(li);
                break;
            case 3:
                System.exit(-1);
                break;
        }
    }

    @Override
    public void AttackerMenu() throws RemoteException {

    }

    @Override
    public void processAttackerOptions() throws RemoteException {

    }

    @Override
    public void DefenderMenu() throws RemoteException {

    }

    @Override
    public void processDefenderOptions() throws RemoteException {

    }

    private void actionMenu(){

    }

    @Override
    public void Register() throws RemoteException {

    }

    @Override
    public void login() throws RemoteException {

    }

    @Override
    public void logout() throws RemoteException {

    }

    @Override
    public void repeat() throws RemoteException {

    }

    private void testEnd(){

    }


    private SocketClient(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
        }catch (Exception e){
            System.err.println("Could not connect to server, exiting");
            System.exit(-1);
        }
        outputStream = socket.getOutputStream();

        while(true){
            if(player == null){
                initialMenu();
            }else{
                actionMenu();
                testEnd();
            }
        }
    }
}
