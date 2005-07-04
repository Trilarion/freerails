/*
 * Created on Apr 13, 2004
 */
package jfreerails.network;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;

/**
 * JUnit test for EchoGameServer.
 * 
 * @author Luke
 * 
 */
public class EchoGameServerTest extends AbstractEchoGameServerTestCase {
	/**
	 * Tests connecting to an EchoGameServer using instances of
	 * InetConnection2Server.
	 */
	public void testConnecting() {
		try {
			assertEquals(0, echoGameServer.countOpenConnections());

			InetConnection2Server con1 = new InetConnection2Server(ipAddress,
					server.getLocalPort());
			InetConnection2Server con2 = new InetConnection2Server(ipAddress,
					server.getLocalPort());
			assertEquals(2, echoGameServer.countOpenConnections());
			con1.writeToServer(new Money(99));
			con1.flush();

			FreerailsSerializable fs = con2.waitForObject();
			assertEquals(new Money(99), fs);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}