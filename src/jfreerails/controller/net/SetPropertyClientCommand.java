/*
 * Created on Apr 18, 2004
 */
package jfreerails.controller.net;

import java.io.Serializable;


/**
 *
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