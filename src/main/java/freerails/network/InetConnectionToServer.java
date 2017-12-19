package freerails.network;

import freerails.world.FreerailsSerializable;

import java.io.IOException;

/**
 * Lets a client send messages to the server over the Internet.
 *
 */
public class InetConnectionToServer extends AbstractInetConnection implements
        ConnectionToServer {
    final String serverDetails;

    /**
     *
     * @param ip
     * @param port
     * @throws IOException
     */
    public InetConnectionToServer(String ip, int port) throws IOException {
        super(ip, port);
        serverDetails = "server at " + ip + ":" + port;
    }

    public FreerailsSerializable[] readFromServer() throws IOException {
        return read();
    }

    public FreerailsSerializable waitForObjectFromServer() throws IOException,
            InterruptedException {
        return waitForObject();
    }

    public void writeToServer(FreerailsSerializable object) throws IOException {
        send(object);
    }

    @Override
    String getThreadName() {
        return "InetConnectionToServer";
    }

    /**
     *
     * @return
     */
    public String getServerDetails() {
        return serverDetails;
    }
}