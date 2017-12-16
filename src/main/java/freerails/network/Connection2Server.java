/*
 * Created on Apr 11, 2004
 */
package freerails.network;

import freerails.world.common.FreerailsSerializable;

import java.io.IOException;

/**
 * Defines the methods a client can use to send messages to the server.
 *
 * @author Luke
 */
public interface Connection2Server {
    /**
     * Returns true if this connection is open.
     */
    boolean isOpen();

    /**
     * Returns an array containing all the objects read from the server since
     * the last time this method or waitForObjectFromServer() was called, if no
     * objects have been received, it returns an empty array rather than
     * blocking.
     */
    FreerailsSerializable[] readFromServer() throws IOException;

    /**
     * Returns the next object read from the server, blocking if non is
     * available.
     */
    FreerailsSerializable waitForObjectFromServer() throws IOException,
            InterruptedException;

    /**
     * Sends the specified object to the server.
     */
    void writeToServer(FreerailsSerializable object) throws IOException;

    /**
     * Disconnect from the server. When this method returns, calling isOpen() on
     * this object returns false <b>and</b> calling isOpen() on the
     * corresponding Connection2Client held by the server also returns false.
     *
     * @throws IOException
     */
    void disconnect() throws IOException;

    /**
     * Flush the underlying stream.
     */
    void flush() throws IOException;

    String getServerDetails();
}