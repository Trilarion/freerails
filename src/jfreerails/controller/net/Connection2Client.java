/*
 * Created on Apr 11, 2004
 */
package jfreerails.controller.net;

import java.io.IOException;
import jfreerails.world.common.FreerailsSerializable;


/**
 *
 *  @author Luke
 *
 */
public interface Connection2Client {
    boolean isOpen();

    FreerailsSerializable[] readFromClient() throws IOException;

    FreerailsSerializable waitForObjectFromClient()
        throws IOException, InterruptedException;

    void writeToClient(FreerailsSerializable object) throws IOException;

    void flush() throws IOException;

    void disconnect() throws IOException;
}