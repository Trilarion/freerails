package freerails.network;

import freerails.controller.MessageToServer;
import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * Request to load a game.
 *
 */
public class LoadGameMessageToServer implements MessageToServer {
    private static final long serialVersionUID = 3256726186552930869L;

    private final int id;
    private final String filename;

    /**
     *
     * @param id
     * @param s
     */
    public LoadGameMessageToServer(int id, String s) {
        this.id = id;
        this.filename = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LoadGameMessageToServer))
            return false;

        final LoadGameMessageToServer loadGameMessageToServer = (LoadGameMessageToServer) o;

        if (id != loadGameMessageToServer.id)
            return false;
        return filename.equals(loadGameMessageToServer.filename);
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
            server.loadgame(filename);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            e.printStackTrace();

            return new MessageStatus(id, false, e.getMessage());
        }
    }
}