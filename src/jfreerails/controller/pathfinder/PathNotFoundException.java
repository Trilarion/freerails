/*
 * Created on Sep 4, 2004
 *
 */
package jfreerails.controller.pathfinder;


/**
 * Thrown when a path cannot be found.
 *
 * @author Luke
 *
 */
public class PathNotFoundException extends Exception {
    public PathNotFoundException(String arg0) {
        super(arg0);
    }
}