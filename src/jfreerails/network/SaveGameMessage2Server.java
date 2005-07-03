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
public class SaveGameMessage2Server implements Message2Server {
    private static final long serialVersionUID = 3257281452725777209L;
	private final int id;
    private final String filename;

    public SaveGameMessage2Server(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    public int getID() {
        return id;
    }

    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.savegame(filename);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            return new MessageStatus(id, false, e.getMessage());
        }
    }
}