/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;


/**
 * Request to load a game.
 *
 *  @author Luke
 *
 */
public class LoadGameServerCommand implements ServerCommand {
    private final int id;
    private final String filename;

    public LoadGameServerCommand(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    public int getID() {
        return id;
    }

    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.loadgame(filename);

            return new CommandStatus(id, true);
        } catch (Exception e) {
            e.printStackTrace();

            return new CommandStatus(id, false, e.getMessage());
        }
    }
}