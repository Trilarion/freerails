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
import junit.framework.TestCase;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Test for NewLocalConnection.
 */
public class LocalConnectionTest extends TestCase {

    private final Serializable[] EmptyArray = new Serializable[0];
    private LocalConnection localConnection;
    private Server server;

    /**
     * sometimes it works ...
     */
    public void testReadFromClient() {
        Serializable[] objectsRead;

        try {
            objectsRead = localConnection.readFromClient();
            assertNotNull(objectsRead);
            assertTrue(Arrays.equals(EmptyArray, objectsRead));

            Money m = new Money(100);
            localConnection.writeToServer(m); // From the client.
            objectsRead = localConnection.readFromClient();

            Serializable[] expectedArray = {m};
            assertTrue(Arrays.equals(expectedArray, objectsRead));
            objectsRead = localConnection.readFromClient();
            assertTrue(Arrays.equals(EmptyArray, objectsRead));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     *
     */
    public void testWait() {
        try {
            Money m = new Money(100);

            localConnection.writeToServer(m);

            // Since we have just added an object, there is no need to wait.
            Object o = localConnection.waitForObjectFromClient();
            assertEquals(m, o);

            localConnection.writeToServer(m);
            o = localConnection.waitForObjectFromServer();

            assertEquals(m, o);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     *
     */
    public void testReadFromServer() {
        Serializable[] objectsRead;

        try {
            objectsRead = localConnection.readFromServer();
            assertNotNull(objectsRead);
            assertTrue(Arrays.equals(EmptyArray, objectsRead));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     *
     */
    public void testClose() {
        try {
            localConnection.disconnect();

            Money m = new Money(100);
            localConnection.writeToClient(m);
            fail();
        } catch (IOException e) {
        }
    }

    /**
     *
     */
    public void testIsOpen() {
        assertTrue(localConnection.isOpen());
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        localConnection = new LocalConnection();
        server = new Server(this.localConnection);

        Thread t = new Thread(server);
        t.start(); // Start the sever thread.
    }

    /**
     * @throws Exception
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.stop(); // Stop the server thread.
    }

    private static class Server implements Runnable {
        private final LocalConnection connection;
        private boolean keepGoing = true;

        private Server(LocalConnection l) {
            connection = l;
        }

        public void run() {
            try {
                boolean result;
                synchronized (this) {
                    result = keepGoing;
                }
                while (result) {
                    Serializable fs = connection
                            .waitForObjectFromClient();
                    connection.writeToClient(fs);
                }
            } catch (Exception e) {
                fail();
            }
        }

        private synchronized void stop() {
            this.keepGoing = false;
        }
    }
}