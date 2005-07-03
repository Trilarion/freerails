/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;


/**
 * Request to start a game on a new map.
 *
 *  @author Luke
 *
 */
public class NewGameMessage2Server implements Message2Server {
    private static final long serialVersionUID = 3256723961743422513L;
	private final int id;
    private final String mapName;

    public NewGameMessage2Server(int id, String s) {
        this.id = id;
        this.mapName = s;
    }

    public int getID() {
        return id;
    }

    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.newGame(mapName);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            return new MessageStatus(id, false, e.getMessage());
        }
    }

    /**
     * TODO This would be better implemented in a config file, or better
     * still dynamically determined by scanning the directory.
     */
    public static String[] getMapNames() {
        return new String[] {"south_america", "small_south_america"};
    }
}