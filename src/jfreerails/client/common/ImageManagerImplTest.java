/*
 * Created on 14-Jan-2005
 *
 */
package jfreerails.client.common;

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
