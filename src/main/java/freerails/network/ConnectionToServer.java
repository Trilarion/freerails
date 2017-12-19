package freerails.network;

import freerails.world.FreerailsSerializable;

import java.io.IOException;

/**
 * Defines the methods a client can use to send messages to the server.
 *
 */
public interface ConnectionToServer {
    /**
     * Returns true if this connection is open.
     * @return 
     */
    boolean isOpen();

    /**
     * Returns an array containing all the objects read from the server since
     * the last time this method or waitForObjectFromServer() was called, if no
     * objects have been received, it returns an empty array rather than
     * blocking.
     * @return 
     * @throws java.io.IOException
     */
    FreerailsSerializable[] readFromServer() throws IOException;

    /**
     * Returns the next object read from the server, blocking if non is
     * available.
     * @return 
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    FreerailsSerializable waitForObjectFromServer() throws IOException,
            InterruptedException;

    /**
     * Sends the specified object to the server.
     * @param object
     * @throws java.io.IOException
     */
    void writeToServer(FreerailsSerializable object) throws IOException;

    /**
     * Disconnect from the server. When this method returns, calling isOpen() on
     * this object returns false <b>and</b> calling isOpen() on the
     * corresponding ConnectionToClient held by the server also returns false.
     *
     * @throws IOException
     */
    void disconnect() throws IOException;

    /**
     * Flush the underlying stream.
     * @throws java.io.IOException
     */
    void flush() throws IOException;

    /**
     *
     * @return
     */
    String getServerDetails();
}