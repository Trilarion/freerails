package freerails.network;

import freerails.world.FreerailsSerializable;

import java.io.IOException;
import java.net.Socket;

/**
 * Lets the server send messages to a client over the Internet.
 *
 */
public class InetConnectionToClient extends AbstractInetConnection implements
        ConnectionToClient {

    /**
     *
     * @param s
     * @throws IOException
     */
    public InetConnectionToClient(Socket s) throws IOException {
        super(s);
    }

    public FreerailsSerializable[] readFromClient() throws IOException {
        return read();
    }

    public FreerailsSerializable waitForObjectFromClient() throws IOException,
            InterruptedException {
        return waitForObject();
    }

    public void writeToClient(FreerailsSerializable object) throws IOException {
        send(object);
    }

    @Override
    String getThreadName() {
        return "InetConnectionToClient";
    }
}