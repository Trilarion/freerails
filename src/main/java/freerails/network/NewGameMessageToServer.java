package freerails.network;

import freerails.controller.MessageToServer;
import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * Request to start a game on a new map.
 *
 */
public class NewGameMessageToServer implements MessageToServer {
    private static final long serialVersionUID = 3256723961743422513L;

    private final int id;

    private final String mapName;

    /**
     *
     * @param id
     * @param s
     */
    public NewGameMessageToServer(int id, String s) {
        this.id = id;
        this.mapName = s;
    }

    /**
     * TODO This would be better implemented in a config file, or better still
     * dynamically determined by scanning the directory.
     * @return 
     */
    public static String[] getMapNames() {
        return new String[]{"South America", "Small South America"};
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
            server.newGame(mapName);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            return new MessageStatus(id, false, e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NewGameMessageToServer))
            return false;

        final NewGameMessageToServer newGameMessageToServer = (NewGameMessageToServer) o;

        if (id != newGameMessageToServer.id)
            return false;
        return mapName.equals(newGameMessageToServer.mapName);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + mapName.hashCode();
        return result;
    }
}