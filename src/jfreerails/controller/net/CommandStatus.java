/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;

import jfreerails.world.common.FreerailsSerializable;


/**
 *  An instance of this class is returned to the client (the server) when a ServerCommand (ClientCommand)
 * is executed by the server (the client).
 * @see ClientCommand
 * @see ServerCommand
 *  @author Luke
 *
 */
public class CommandStatus implements FreerailsSerializable {
    private final int id;
    private final String reason;
    private final boolean successful;

    CommandStatus(int id, boolean successful, String reason) {
        this.id = id;
        this.reason = reason;
        this.successful = successful;
    }

    CommandStatus(int id, boolean successful) {
        this.id = id;
        this.reason = null;
        this.successful = successful;
    }

    /** Returns the id of the command whose status this object stores.*/
    public int getId() {
        return id;
    }

    /** Returns the reason the command failed, may be null.*/
    public String getReason() {
        return reason;
    }

    /** True if the command was successfully executed.*/
    public boolean isSuccessful() {
        return successful;
    }
}