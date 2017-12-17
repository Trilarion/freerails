/*
 * Created on Apr 18, 2004
 */
package freerails.network;

import freerails.controller.ClientControlInterface;
import freerails.controller.ClientControlInterface.ClientProperty;
import freerails.controller.Message2Client;
import freerails.controller.MessageStatus;
import freerails.world.common.FreerailsSerializable;

/**
 * A Message2Client that lets the server set a property (for example, the list
 * of saved games available) on a client.
 *
 * @author Luke
 */
public class SetPropertyMessage2Client implements Message2Client {
    private static final long serialVersionUID = 3544392521746034740L;

    private final int id;

    private final ClientProperty key;

    private final FreerailsSerializable value;

    public SetPropertyMessage2Client(int id, ClientProperty key,
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
        if (!(o instanceof SetPropertyMessage2Client))
            return false;

        final SetPropertyMessage2Client setPropertyMessage2Client = (SetPropertyMessage2Client) o;

        if (id != setPropertyMessage2Client.id)
            return false;
        if (!key.equals(setPropertyMessage2Client.key))
            return false;
        return value.equals(setPropertyMessage2Client.value);
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