/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;


/**
 * A request to save the game.
 *
 *  @author Luke
 *
 */
public class SaveGameServerCommand implements ServerCommand {
    private final int id;
    private final String filename;

    public SaveGameServerCommand(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    public int getID() {
        return id;
    }

    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.savegame(filename);

            return new CommandStatus(id, true);
        } catch (Exception e) {
            return new CommandStatus(id, false, e.getMessage());
        }
    }
}