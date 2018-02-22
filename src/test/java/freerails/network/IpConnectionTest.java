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
 * Test for IpConnection.
 */
public class IpConnectionTest extends AbstractEchoGameServerTestCase {

    /**
     *
     */
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.getNumberOpenConnections());

            IpConnection ipConnection = new IpConnection(ipAddress, PORT);
            ipConnection.initialize();
            assertEquals(1, echoGameServer.getNumberOpenConnections());

            IpConnection ipConnection2 = new IpConnection(ipAddress, PORT);
            ipConnection2.initialize();
            assertEquals(2, echoGameServer.getNumberOpenConnections());
        } catch (Exception e) {
            fail();
        }
    }
}