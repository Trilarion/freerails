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
public class NewGameServerCommand implements ServerCommand {
    private final int id;
    private final String mapName;

    public NewGameServerCommand(int id, String s) {
        this.id = id;
        this.mapName = s;
    }

    public int getID() {
        return id;
    }

    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.newGame(mapName);

            return new CommandStatus(id, true);
        } catch (Exception e) {
            return new CommandStatus(id, false, e.getMessage());
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