package jfreerails.controller.net;

import java.io.IOException;
import jfreerails.world.common.FreerailsSerializable;


/**
 *   Lets a client send messages to the server over the Internet.
 *  @author Luke
 *
 */
public class InetConnection2Server extends AbstractInetConnection
    implements Connection2Server {
    public InetConnection2Server(String ip, int port) throws IOException {
        super(ip, port);
    }

    public FreerailsSerializable[] readFromServer() throws IOException {
        return read();
    }

    public FreerailsSerializable waitForObjectFromServer()
        throws IOException, InterruptedException {
        return waitForObject();
    }

    public void writeToServer(FreerailsSerializable object)
        throws IOException {
        send(object);
    }

    String getThreadName() {
        return "InetConnection2Server";
    }
}