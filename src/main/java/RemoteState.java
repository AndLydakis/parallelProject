import java.rmi.RemoteException;

/**
 * The remote interface for the game state
 */
public interface RemoteState extends java.rmi.Remote {
    /**
     * Log a player in the game
     *
     * @param username logs a player into the game and return it
     * @return the player logged on or a new player
     * @throws RemoteException if rmi fails if rmi fails
     */
    RemotePlayer login(String username) throws RemoteException;

    /**
     * Log out a player
     *
     * @param username the username of the player
     * @return true if the logout was successful, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean logout(String username) throws RemoteException;

    /**
     * Attempt to register a new username
     *
     * @param username the player's username
     * @param role     the player's role
     * @return true if the registration was successful, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean register(String username, int role) throws RemoteException;

    /**
     * Attempt to register a new username with additional stats
     *
     * @param username  the player's username
     * @param role      the player's role
     * @param score     the player's score
     * @param credits   the player's credits
     * @param primary   the player's primary ability rating
     * @param secondary the player's secondary ability rating
     * @param items     the player's number of items
     * @return true if the registration was successful, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean register(String username, int role, int score, int credits, int primary, int secondary, int items) throws RemoteException;

    /**
     * Check if the game is over
     *
     * @return true if the game is still going, false otherwise
     * @throws RemoteException if rmi fails
     */
    boolean isAlive() throws RemoteException;

    /**
     * Request to apply the primary ability of the player on a block
     *
     * @param user  the player's name
     * @param role  the player's role
     * @param block the target block
     * @return the result of the request
     * @throws RemoteException if rmi fails
     */
    int requestPrimary(String user, int role, String block) throws RemoteException;

    /**
     * Request to apply the secondary ability of the player on a block
     *
     * @param user  the player's name
     * @param role  the player's role
     * @param block the target block
     * @return the result of the request
     * @throws RemoteException if rmi fails
     */
    int requestSecondary(String user, int role, String block) throws RemoteException;

    /**
     * Request to boost player's speed
     *
     * @param user the player's name
     * @param role the player's role
     * @return the result of the request
     * @throws RemoteException if rmi fails
     */
    int requestBoost(String user, int role) throws RemoteException;

    /**
     * Request to level up the player's primary ability
     *
     * @param user the player's name
     * @param role the player's role
     * @return the primary ability's new value, or the credits needed
     * to upgrade it
     * @throws RemoteException if rmi fails
     */
    int levelPrimary(String user, int role) throws RemoteException;

    /**
     * Request to level up the player's secondary ability
     *
     * @param user the player's name
     * @param role the player's role
     * @return the secondary ability's new value, or the credits needed
     * to upgrade it
     * @throws RemoteException if rmi fails
     */
    int levelSecondary(String user, int role) throws RemoteException;

    /**
     * Request to buy items
     *
     * @param user the player's name
     * @param role the player's role
     * @return the new number of items, or the credits needed for a purchase
     * @throws RemoteException if rmi fails
     */
    int buy(String user, int role) throws RemoteException;

    /**
     * Print the status of the state
     *
     * @return 1 if attackers won, -1 if defenders won, 0 if
     * the game is still ongoing, 666 if the game crashed
     * @throws RemoteException if rmi fails
     */
    int printStatus() throws RemoteException;

    /**
     * Print the time left
     *
     * @throws RemoteException if rmi fails
     */
    void printTimeLeft() throws RemoteException;

    /**
     * Get the available targets for the player
     *
     * @return a string containing the available targets
     * @throws RemoteException if rmi fails
     */
    String getTargets() throws RemoteException;

    /**
     * Print a player as a String
     *
     * @param player the player to print
     * @param role   the role of the player
     * @return 1 if the status was retrieved successfully, -1 otherwise
     * @throws RemoteException if rmi fails
     */
    String printPlayer(String player, int role) throws RemoteException;

    /**
     * Process a user request
     *
     * @param request the user request
     * @return a string containing the server's response
     * @throws RemoteException if rmi fails
     */
    String parseRequest(String request) throws RemoteException;

    /**
     * Print the top 10 players
     *
     * @return the top 10 players as a string
     * @throws RemoteException if rmi fails
     */
    String printLeaderBoards() throws RemoteException;
}
