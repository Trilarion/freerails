/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import java.io.Serializable;


/**
 *  A Message2Client that lets the server set a property (for example, the list
 * of saved games available) on a client.
 *  @author Luke
 *
 */
public class SetPropertyMessage2Client implements Message2Client {
    private static final long serialVersionUID = 3544392521746034740L;
	private final int id;
    private final String key;
    private final Serializable value;

    public SetPropertyMessage2Client(int id, String key, Serializable value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public MessageStatus execute(ClientControlInterface client) {
        client.setProperty(key, value);

        return new MessageStatus(id, true);
    }

    public int getID() {
        return id;
    }
}