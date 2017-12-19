package freerails.network;

import freerails.controller.ClientControlInterface;
import freerails.controller.MessageToClient;
import freerails.controller.MessageStatus;
import freerails.util.Immutable;
import freerails.world.top.World;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 *
 */
@Immutable
public class SetWorldMessageToClient implements MessageToClient {
    private static final long serialVersionUID = 3257570619972269362L;

    private final int id;

    private final World world;

    /**
     * Note, makes a defensive copy of the world object passed to it.
     * @param id
     * @param world
     */
    public SetWorldMessageToClient(int id, World world) {
        this.id = id;
        this.world = world.defensiveCopy();
    }

    public MessageStatus execute(ClientControlInterface client) {
        client.setGameModel(world.defensiveCopy());

        return new MessageStatus(id, true);
    }

    public int getID() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SetWorldMessageToClient))
            return false;

        final SetWorldMessageToClient setWorldMessageToClient = (SetWorldMessageToClient) o;

        if (id != setWorldMessageToClient.id)
            return false;
        return world.equals(setWorldMessageToClient.world);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + world.hashCode();
        return result;
    }
}