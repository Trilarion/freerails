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
public class LoadGameMessage2Server implements Message2Server {
    private static final long serialVersionUID = 3256726186552930869L;
	private final int id;
    private final String filename;

    public LoadGameMessage2Server(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    public int getID() {
        return id;
    }

    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.loadgame(filename);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            e.printStackTrace();

            return new MessageStatus(id, false, e.getMessage());
        }
    }
}