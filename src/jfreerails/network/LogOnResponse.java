/*
 * Created on Apr 17, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;


/**
 * Stores the result of a request to log onto the server.
 *  @author Luke
 *
 */
public class LogOnResponse implements FreerailsSerializable {
    private final boolean successful;
    private final int playerNumber;
    private final String message;

    private LogOnResponse(boolean success, int i, String s) {
        this.successful = success;
        this.playerNumber = i;
        this.message = s;
    }

    public static LogOnResponse accepted(int playerNumber) {
        return new LogOnResponse(true, playerNumber, null);
    }

    public static LogOnResponse rejected(String reason) {
        return new LogOnResponse(false, -1, reason);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccessful() {
        return successful;
    }
}