/*
 * Created on Apr 14, 2004
 */
package freerails.controller;

import freerails.world.common.FreerailsSerializable;

/**
 * An instance of this class is returned to the client (the server) when a
 * Message2Server (Message2Client) is executed by the server (the client).
 *
 * @author Luke
 * @see Message2Client
 * @see Message2Server
 */
public class MessageStatus implements FreerailsSerializable {
    private static final long serialVersionUID = 3257285842216103987L;

    private final int id;
    private final String reason;
    private final boolean successful;

    /**
     *
     * @param id
     * @param successful
     * @param reason
     */
    public MessageStatus(int id, boolean successful, String reason) {
        this.id = id;
        this.reason = reason;
        this.successful = successful;
    }

    /**
     *
     * @param id
     * @param successful
     */
    public MessageStatus(int id, boolean successful) {
        this.id = id;
        this.reason = null;
        this.successful = successful;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MessageStatus))
            return false;

        final MessageStatus messageStatus = (MessageStatus) o;

        if (id != messageStatus.id)
            return false;
        if (successful != messageStatus.successful)
            return false;
        return reason != null ? reason.equals(messageStatus.reason) : messageStatus.reason == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + (reason != null ? reason.hashCode() : 0);
        result = 29 * result + (successful ? 1 : 0);
        return result;
    }

    /**
     * Returns the id of the command whose status this object stores.
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the reason the command failed, may be null.
     * @return 
     */
    public String getReason() {
        return reason;
    }

    /**
     * True if the command was successfully executed.
     * @return 
     */
    public boolean isSuccessful() {
        return successful;
    }
}