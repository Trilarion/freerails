/*
 * Created on Apr 17, 2004
 */
package jfreerails.controller.net;

import jfreerails.world.common.FreerailsSerializable;


/**
 *
 *  @author Luke
 *
 */
public class LogOnRequest implements FreerailsSerializable {
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