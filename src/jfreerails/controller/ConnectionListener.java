package jfreerails.controller;

public interface ConnectionListener {
    /**
     * Indicates that the specified connection was closed by the remote side
     */
    public void connectionClosed(ConnectionToServer c); 

    /**
     * Indicates that the state of the connection has changed.
     */
    public void connectionStateChanged(ConnectionToServer c);
}
