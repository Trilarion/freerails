/*
 * Created on 02-Jul-2005
 *
 */
package freerails.util;

import junit.framework.TestCase;

import java.awt.*;
import java.io.Serializable;

public class UtilsTest extends TestCase {

    public void testEqualsBySerialization() {

        Serializable a = new Point(10, 10);
        Serializable b = new Point(10, 10);
        Serializable c = new Point(30, 10);

        assertTrue(Utils.equalsBySerialization(a, b));
        assertTrue(Utils.equalsBySerialization(a, a));
        assertTrue(Utils.equalsBySerialization(b, b));
        assertTrue(Utils.equalsBySerialization(c, c));
        assertFalse(Utils.equalsBySerialization(a, c));

    }

}
