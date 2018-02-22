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

import freerails.model.finances.Money;

import java.io.Serializable;

/**
 * Test for EchoGameServer.
 */
public class EchoGameServerTest extends AbstractEchoGameServerTestCase {

    /**
     * Tests connecting to an EchoGameServer using instances of IpConnectionToServer.
     */
    public void testConnecting() {
        try {
            assertEquals(0, echoGameServer.getNumberOpenConnections());

            IpConnectionToServer con1 = new IpConnectionToServer(ipAddress, PORT);
            IpConnectionToServer con2 = new IpConnectionToServer(ipAddress, PORT);
            assertEquals(2, echoGameServer.getNumberOpenConnections());
            con1.writeToServer(new Money(99));

            Serializable fs = con2.waitForObject();
            assertEquals(new Money(99), fs);
        } catch (Exception e) {
            fail();
        }
    }
}