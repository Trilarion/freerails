/*
 * Created on 20-Mar-2003
 *
 */
package freerails.world.top;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * JUnit test.
 *
 */
public class SKEYTest extends TestCase {

    /**
     *
     */
    public void testGetNumberOfKeys() {
        assertTrue(SKEY.getNumberOfKeys() > 5);
    }

    /**
     *
     */
    public void testThatAllTheFieldsDefinedInSKEYAreInstancesOFSKEY() {
        Field[] fields = SKEY.class.getFields();

        for (Field field : fields) {
            String name = field.getName();
            int modifiers = field.getModifiers();

            if (!name.equals("shared")) {
                assertTrue("All the fields of SKEY should be static", Modifier
                        .isStatic(modifiers));
            }

            assertTrue("All the fields of SKEY should be public", Modifier
                    .isPublic(modifiers));
            assertTrue("All the fields of SKEY should be final", Modifier
                    .isFinal(modifiers));

            try {
                if (Modifier.isStatic(modifiers)) {
                    Object o = field.get(null);
                    assertTrue("All the fields of SKEY should be instances of"
                            + " SKEY", o instanceof SKEY);
                }
            } catch (IllegalAccessException e) {
                assertTrue(false);
            }
        }
    }
}