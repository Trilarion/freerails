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
	private static final long serialVersionUID = 4121409601112717368L;

	public PathNotFoundException(String arg0) {
		super(arg0);
	}
}