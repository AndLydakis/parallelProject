import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

/**
 * Created by andreas on 4/27/17.
 */
public class AdminMenu extends Thread {
    public final LocalState state;

    public void process(String in) {
        String tokens[] = in.split(" ");
        try {
            switch (tokens[0]) {
                case "SET": {
                    switch (tokens[1]) {
                        case "ATK": {
                            state.setAtk(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "REP": {
                            state.setRep(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "SHIELD": {
                            state.setShields(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "BOMB": {
                            state.setBombs(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "ASPD": {
                            state.setSpeed(tokens[2], 1, Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "DSPD": {
                            state.setSpeed(tokens[2], 0, Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "CREDITS": {
                            state.setCredits(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "LVLAR": {
                            state.setLevelAr(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "LVLRR": {
                            state.setLevelRr(tokens[2], Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "ALVLSPD": {
                            state.setLevelSpd(tokens[2], 1, Integer.parseInt(tokens[3]));
                            break;
                        }
                        case "DLVLSPD": {
                            state.setLevelSpd(tokens[2], 0, Integer.parseInt(tokens[3]));
                            break;
                        }
                    }
                }
                case "LIST": {
                    System.err.println("Player List");
                    state.printPlayers();
                    break;
                }
                case "SAVE" :{
                    try {
                        FileOutputStream fileOut =
                                new FileOutputStream("/tmp/"+tokens[1]+".ser");
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(state);
                        out.close();
                        fileOut.close();
                        System.out.printf("Serialized data is saved in /tmp/"+tokens[1]+".ser");
                    }catch(IOException i) {
                        i.printStackTrace();
                    }
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Scanner reader = new Scanner(System.in);
        String input;
        do {
            input = reader.nextLine();
            process(input);
        } while (input != "Q");
    }

    public AdminMenu(LocalState state) {
        this.state = state;
    }
}
