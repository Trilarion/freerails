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
public interface Connection2Server {
    boolean isOpen();

    FreerailsSerializable[] readFromServer() throws IOException;

    FreerailsSerializable waitForObjectFromServer()
        throws IOException, InterruptedException;

    void writeToServer(FreerailsSerializable object) throws IOException;

    void disconnect() throws IOException;

    void flush() throws IOException;
}