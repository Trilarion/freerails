/*
 * Created on Apr 18, 2004
 */
package jfreerails.controller.net;


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
}