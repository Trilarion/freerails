/*
 * Created on Apr 18, 2004
 */
package freerails.network;

import freerails.controller.Message2Server;
import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * A request to save the game.
 *
 * @author Luke
 */
public class SaveGameMessage2Server implements Message2Server {
    private static final long serialVersionUID = 3257281452725777209L;

    private final int id;
    private final String filename;

    /**
     *
     * @param id
     * @param s
     */
    public SaveGameMessage2Server(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SaveGameMessage2Server))
            return false;

        final SaveGameMessage2Server saveGameMessage2Server = (SaveGameMessage2Server) o;

        if (id != saveGameMessage2Server.id)
            return false;
        return filename.equals(saveGameMessage2Server.filename);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + filename.hashCode();
        return result;
    }

    /**
     *
     * @return
     */
    public int getID() {
        return id;
    }

    /**
     *
     * @param server
     * @return
     */
    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.savegame(filename);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            return new MessageStatus(id, false, e.getMessage());
        }
    }
}