/*
 * Created on 20-Mar-2003
 *
 */
package jfreerails.world.top;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import junit.framework.TestCase;


/**
 * @author Luke
 *
 */
public class SKEYTest extends TestCase {
    public void testGetNumberOfKeys() {
        assertTrue(SKEY.getNumberOfKeys() > 5);
    }

    public void testThatAllTheFieldsDefinedInSKEYAreInstancesOFSKEY() {
        Field[] fields = SKEY.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            int modifiers = fields[i].getModifiers();

            if (!name.equals("shared")) {
                assertTrue("All the fields of SKEY should be static",
                    Modifier.isStatic(modifiers));
            }

            assertTrue("All the fields of SKEY should be public",
                Modifier.isPublic(modifiers));
            assertTrue("All the fields of SKEY should be final",
                Modifier.isFinal(modifiers));

            try {
                if (Modifier.isStatic(modifiers)) {
                    Object o = fields[i].get(null);
                    assertTrue("All the fields of SKEY should be instances of" +
                        " SKEY", o instanceof SKEY);
                }
            } catch (IllegalAccessException e) {
                assertTrue(false);
            }
        }
    }
}