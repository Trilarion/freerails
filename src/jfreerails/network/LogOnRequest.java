/*
 * Created on Apr 17, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;


/**
 *  A client sends an instance of this class to the server when it
 *  wishes to log on.
 *  @author Luke
 *
 */
public class LogOnRequest implements FreerailsSerializable {
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