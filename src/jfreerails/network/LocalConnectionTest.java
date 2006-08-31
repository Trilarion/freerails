/*
 * Created on Apr 11, 2004
 */
package jfreerails.network;

import java.io.IOException;
import java.util.Arrays;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import junit.framework.TestCase;

/**
 * JUnit test for NewLocalConnection.
 * 
 * @author Luke
 * 
 */
public class LocalConnectionTest extends TestCase {
	private static class Server implements Runnable {
		private boolean keepGoing = true;

		private final LocalConnection connection;

		public Server(LocalConnection l) {
			connection = l;
		}

		public void run() {
			try {
				while (isKeepGoing()) {
					FreerailsSerializable fs = connection
							.waitForObjectFromClient();
					connection.writeToClient(fs);
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		}

		private synchronized boolean isKeepGoing() {
			return keepGoing;
		}

		public synchronized void stop() {
			this.keepGoing = false;
		}
	}

	private LocalConnection localConnection;

	private final FreerailsSerializable[] EmptyArray = new FreerailsSerializable[0];

	private Server server;

	public void testReadFromClient() {
		FreerailsSerializable[] objectsRead;

		try {
			objectsRead = localConnection.readFromClient();
			assertNotNull(objectsRead);
			assertTrue(Arrays.equals(EmptyArray, objectsRead));

			Money m = new Money(100);
			localConnection.writeToServer(m); // From the client.
			objectsRead = localConnection.readFromClient();

			FreerailsSerializable[] expectedArray = { m };
			assertTrue(Arrays.equals(expectedArray, objectsRead));
			objectsRead = localConnection.readFromClient();
			assertTrue(Arrays.equals(EmptyArray, objectsRead));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

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
			e.printStackTrace();
			fail();
		}
	}

	public void testReadFromServer() {
		FreerailsSerializable[] objectsRead;

		try {
			objectsRead = localConnection.readFromServer();
			assertNotNull(objectsRead);
			assertTrue(Arrays.equals(EmptyArray, objectsRead));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testClose() {
		try {
			localConnection.disconnect();

			Money m = new Money(100);
			localConnection.writeToClient(m);
			fail();
		} catch (IOException e) {
		}
	}

	public void testIsOpen() {
		assertTrue(localConnection.isOpen());
	}

	@Override
	protected void setUp() throws Exception {
		localConnection = new LocalConnection();
		server = new Server(this.localConnection);

		Thread t = new Thread(server);
		t.start(); // Start the sever thread.
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		server.stop(); // Stop the server thread.
	}
}