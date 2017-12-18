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

    private LogOnResponse(boolean success, int i, String s) {
        this.successful = success;
        this.playerNumber = i;
        this.message = s;
    }

    /**
     *
     * @param playerNumber
     * @return
     */
    public static LogOnResponse accepted(int playerNumber) {
        return new LogOnResponse(true, playerNumber, null);
    }

    /**
     *
     * @param reason
     * @return
     */
    public static LogOnResponse rejected(String reason) {
        return new LogOnResponse(false, -1, reason);
    }

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
        return message != null ? message.equals(logOnResponse.message) : logOnResponse.message == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (successful ? 1 : 0);
        result = 29 * result + playerNumber;
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     *
     * @return
     */
    public int getPlayerID() {
        return playerNumber;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return
     */
    public boolean isSuccessful() {
        return successful;
    }
}