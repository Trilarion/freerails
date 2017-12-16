/*
 * Created on Apr 17, 2004
 */
package freerails.network;

import freerails.world.common.FreerailsSerializable;

/**
 * Stores the result of a request to log onto the server.
 *
 * @author Luke
 */
public class LogOnResponse implements FreerailsSerializable {
    private static final long serialVersionUID = 3690479099844311344L;

    private final boolean successful;

    private final int playerNumber;

    private final String message;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LogOnResponse))
            return false;

        final LogOnResponse logOnResponse = (LogOnResponse) o;

        if (playerNumber != logOnResponse.playerNumber)
            return false;
        if (successful != logOnResponse.successful)
            return false;
        if (message != null ? !message.equals(logOnResponse.message)
                : logOnResponse.message != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (successful ? 1 : 0);
        result = 29 * result + playerNumber;
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

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

    public int getPlayerID() {
        return playerNumber;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccessful() {
        return successful;
    }
}