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

/**
 * Junit test for NewInetConnection.
 */
public class ConnectionTest extends AbstractEchoGameServerTestCase {

    /**
     *
     */
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.countOpenConnections());

            Connection connection = new Connection(ipAddress, server
                    .getLocalPort());
            connection.open();
            assertEquals(1, echoGameServer.countOpenConnections());

            Connection connection2 = new Connection(ipAddress, server
                    .getLocalPort());
            connection2.open();
            assertEquals(2, echoGameServer.countOpenConnections());
        } catch (Exception e) {
            fail();
        }
    }
}