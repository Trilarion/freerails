/*
 * Created on Apr 14, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;


/**
 *  An instance of this class is returned to the client (the server) when a Message2Server (Message2Client)
 * is executed by the server (the client).
 * @see Message2Client
 * @see Message2Server
 *  @author Luke
 *
 */
public class MessageStatus implements FreerailsSerializable {
    private static final long serialVersionUID = 3257285842216103987L;
	private final int id;
    private final String reason;
    private final boolean successful;

    MessageStatus(int id, boolean successful, String reason) {
        this.id = id;
        this.reason = reason;
        this.successful = successful;
    }

    MessageStatus(int id, boolean successful) {
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