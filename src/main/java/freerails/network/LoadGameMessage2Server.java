/*
 * Created on Apr 18, 2004
 */
package freerails.network;

import freerails.controller.Message2Server;
import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * Request to load a game.
 *
 * @author Luke
 */
public class LoadGameMessage2Server implements Message2Server {
    private static final long serialVersionUID = 3256726186552930869L;

    private final int id;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LoadGameMessage2Server))
            return false;

        final LoadGameMessage2Server loadGameMessage2Server = (LoadGameMessage2Server) o;

        if (id != loadGameMessage2Server.id)
            return false;
        if (!filename.equals(loadGameMessage2Server.filename))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + filename.hashCode();
        return result;
    }

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