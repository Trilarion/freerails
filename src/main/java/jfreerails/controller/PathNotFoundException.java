/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.controller;

/**
 * Thrown when a path cannot be found.
 * 
 * @author Luke
 * 
 */
public class PathNotFoundException extends Exception {
    /**
     * 
     */
    public PathNotFoundException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public PathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public PathNotFoundException(Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = 4121409601112717368L;

    public PathNotFoundException(String arg0) {
        super(arg0);
    }
}