/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.network;

import java.io.IOException;
import java.io.Serializable;

/**
 * Lets a client send messages to the server over the Internet.
 */
public class IpConnectionToServer extends AbstractIpConnection implements ConnectionToServer {

    private final String serverDetails;

    // TODO use java.net.InetSocketAdress instead?
    /**
     * @param ip
     * @param port
     * @throws IOException
     */
    public IpConnectionToServer(String ip, int port) throws IOException {
        super(ip, port);
        serverDetails = "server at " + ip + ':' + port;
    }

    public Serializable[] readFromServer() throws IOException {
        return read();
    }

    public Serializable waitForObjectFromServer() throws IOException, InterruptedException {
        return waitForObject();
    }

    public void writeToServer(Serializable object) throws IOException {
        send(object);
    }

    @Override
    String getThreadName() {
        return "InetConnectionToServer";
    }

    /**
     * @return
     */
    public String getServerDetails() {
        return serverDetails;
    }
}