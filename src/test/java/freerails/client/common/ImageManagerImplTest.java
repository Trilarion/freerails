package freerails.client.common;


import junit.framework.TestCase;

/**
 * @author Luke
 * 
 */
public class ImageManagerImplTest extends TestCase {

    public void testIsValid() {
        assertTrue(ImageManagerImpl.isValid("cursor/infomode.png"));
        assertFalse(ImageManagerImpl.isValid("/cursor/infomode.png"));
    }

}
