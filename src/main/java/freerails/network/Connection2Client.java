/*
 * Created on Apr 11, 2004
 */
package freerails.network;

import freerails.world.common.FreerailsSerializable;

import java.io.IOException;

/**
 * Defines the methods the server can use to send messages to the client.
 *
 * @author Luke
 */
public interface Connection2Client {
    /**
     * Returns true if this connection is open.
     * @return 
     */
    boolean isOpen();

    /**
     * Returns an array containing all the objects read from the client since
     * the last time this method or waitForObjectFromClient() was called, if no
     * objects have been received, it returns an empty array rather than
     * blocking.
     * @return 
     * @throws java.io.IOException
     */
    FreerailsSerializable[] readFromClient() throws IOException;

    /**
     * Returns the next object read from the client, blocking if non is
     * available.
     * @return 
     * @throws java.io.IOException 
     * @throws java.lang.InterruptedException 
     */
    FreerailsSerializable waitForObjectFromClient() throws IOException,
            InterruptedException;

    /**
     * Sends the specified object to the client.
     * @param object
     * @throws java.io.IOException
     */
    void writeToClient(FreerailsSerializable object) throws IOException;

    /**
     * Flush the underlying stream.
     * @throws java.io.IOException
     */
    void flush() throws IOException;

    /**
     * Disconnect from the client. When this method returns, calling isOpen() on
     * this object returns false <b>and</b> calling isOpen() on the
     * corresponding Connection2Server held by the client also returns false.
     *
     * @throws IOException if
     */
    void disconnect() throws IOException;
}