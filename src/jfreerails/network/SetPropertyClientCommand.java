/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import java.io.Serializable;


/**
 *  A ClientCommand that lets the server set a property (for example, the list
 * of saved games available) on a client.
 *  @author Luke
 *
 */
public class SetPropertyClientCommand implements ClientCommand {
    private final int id;
    private final String key;
    private final Serializable value;

    public SetPropertyClientCommand(int id, String key, Serializable value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public CommandStatus execute(ClientControlInterface client) {
        client.setProperty(key, value);

        return new CommandStatus(id, true);
    }

    public int getID() {
        return id;
    }
}