/*
 * Created on 24 Jul 2006
 *
 */
package freerails.network;

import freerails.controller.MessageToServer;
import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * Tells the server to check the filesystem for changes to the available new
 * maps and saved games.
 *
 */
public class RefreshListOfGamesMessageToServer implements MessageToServer {

    private static final long serialVersionUID = -8745171955732354168L;
    private final int id;

    /**
     *
     * @param id
     */
    public RefreshListOfGamesMessageToServer(final int id) {
        super();
        this.id = id;
    }

    /**
     *
     * @param server
     * @return
     */
    public MessageStatus execute(ServerControlInterface server) {
        server.refreshSavedGames();
        return new MessageStatus(id, true);
    }

    /**
     *
     * @return
     */
    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RefreshListOfGamesMessageToServer other = (RefreshListOfGamesMessageToServer) obj;
        return id == other.id;
    }

}
