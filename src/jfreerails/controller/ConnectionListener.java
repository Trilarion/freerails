package jfreerails.controller;

public interface ConnectionListener {
    /**
     * Indicates that the specified connection was closed by the remote side
     */
    public void connectionClosed(ConnectionToServer c);

    /**
     * Indicates that the state or number of players of the connection has
     * changed.
     */
    public void connectionStateChanged(ConnectionToServer c);

    /**
     * process a ServerCommand sent by the remote side
     */
    public void processServerCommand(ConnectionToServer c, ServerCommand s);
}