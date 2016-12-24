/*
 * Created on Apr 17, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;

/**
 * A client sends an instance of this class to the server when it wishes to log
 * on.
 * 
 * @author Luke
 * 
 */
public class LogOnRequest implements FreerailsSerializable {
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
        if (username != null ? !username.equals(logOnRequest.username)
                : logOnRequest.username != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (username != null ? username.hashCode() : 0);
        result = 29 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    private static final long serialVersionUID = 3257854263924240949L;

    private final String username;

    private final String password;

    public LogOnRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}