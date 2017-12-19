package freerails.network;

import freerails.controller.ClientControlInterface;
import freerails.controller.ClientControlInterface.ClientProperty;
import freerails.controller.MessageToClient;
import freerails.controller.MessageStatus;
import freerails.world.FreerailsSerializable;

/**
 * A MessageToClient that lets the server set a property (for example, the list
 * of saved games available) on a client.
 *
 */
public class SetPropertyMessageToClient implements MessageToClient {
    private static final long serialVersionUID = 3544392521746034740L;

    private final int id;

    private final ClientProperty key;

    private final FreerailsSerializable value;

    /**
     *
     * @param id
     * @param key
     * @param value
     */
    public SetPropertyMessageToClient(int id, ClientProperty key,
                                      FreerailsSerializable value) {
        if (null == key || null == value)
            throw new NullPointerException();
        this.id = id;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SetPropertyMessageToClient))
            return false;

        final SetPropertyMessageToClient setPropertyMessageToClient = (SetPropertyMessageToClient) o;

        if (id != setPropertyMessageToClient.id)
            return false;
        if (!key.equals(setPropertyMessageToClient.key))
            return false;
        return value.equals(setPropertyMessageToClient.value);
    }

    public MessageStatus execute(ClientControlInterface client) {
        client.setProperty(key, value);

        return new MessageStatus(id, true);
    }

    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + key.hashCode();
        result = 29 * result + value.hashCode();
        return result;
    }
}