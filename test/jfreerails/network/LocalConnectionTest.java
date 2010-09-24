/*
 * Created on Apr 11, 2004
 */
package jfreerails.network;

import java.util.Arrays;
import jfreerails.network.LocalConnection;

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

    
   
    
    private LocalConnection localConnection;
    private final FreerailsSerializable[] EmptyArray = new FreerailsSerializable[0];
    

    public void testReadFromClient() {
        
        FreerailsSerializable[] objectsRead;

        try {
            objectsRead = localConnection.readFromClient();
            assertNotNull(objectsRead);
            assertTrue(Arrays.equals(EmptyArray, objectsRead));

            Money m = new Money(100);
            localConnection.writeToServer(m); // From the client.
            objectsRead = localConnection.readFromClient();

            FreerailsSerializable[] expectedArray = {m};
            assertEquals(expectedArray.length, objectsRead.length);
            assertTrue(Arrays.equals(expectedArray, objectsRead));
            objectsRead = localConnection.readFromClient();
            assertTrue(Arrays.equals(EmptyArray, objectsRead));
        } catch (Exception e) {
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

            localConnection.writeToClient(m);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
        }
    }

    public void testIsOpen() {
        assertTrue(localConnection.isOpen());
    }

    
    @Override
    protected void setUp() throws Exception {
        localConnection = new LocalConnection();
        
    }

    
}
