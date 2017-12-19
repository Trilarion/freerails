/*
 * Created on Sep 4, 2004
 *
 */
package freerails.controller;

/**
 * Thrown when a path cannot be found.
 *
 */
public class PathNotFoundException extends Exception {
    private static final long serialVersionUID = 4121409601112717368L;

    /**
     *
     */
    public PathNotFoundException() {
        super();
    }

    /**
     * @param message message
     * @param cause   cause
     */
    public PathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause cause
     */
    public PathNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     *
     * @param arg0
     */
    public PathNotFoundException(String arg0) {
        super(arg0);
    }
}