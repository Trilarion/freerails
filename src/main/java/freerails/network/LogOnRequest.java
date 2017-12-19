package freerails.network;

import freerails.world.FreerailsSerializable;

/**
 * A client sends an instance of this class to the server when it wishes to log
 * on.
 *
 */
public class LogOnRequest implements FreerailsSerializable {
    private static final long serialVersionUID = 3257854263924240949L;
    private final String username;
    private final String password;

    /**
     *
     * @param username
     * @param password
     */
    public LogOnRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LogOnRequest))
            return false;

        final LogOnRequest logOnRequest = (LogOnRequest) o;

        if (password != null ? !password.equals(logOnRequest.password)
                : logOnRequest.password != null)
            return false;
        return username != null ? username.equals(logOnRequest.username) : logOnRequest.username == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (username != null ? username.hashCode() : 0);
        result = 29 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @return
     */
    public String getUsername() {
        return username;
    }
}