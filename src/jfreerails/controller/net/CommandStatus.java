/*
 * Created on Apr 14, 2004
 */
package jfreerails.controller.net;

import jfreerails.world.common.FreerailsSerializable;


/**
 *
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

    public int getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public boolean isSuccessful() {
        return successful;
    }
}