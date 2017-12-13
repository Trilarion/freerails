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
public class KEYTest extends TestCase {
    public void testGetNumberOfKeys() {
        //There were 10 keys when a wrote this test,
        //but I expect the number to increase.
        assertTrue(KEY.getNumberOfKeys() > 9);
    }

    public void testThatAllTheFieldsDefinedInKEYAreInstancesOFKEY() {
        Field[] fields = KEY.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            int modifiers = fields[i].getModifiers();

            if (!name.equals("shared")) {
                assertTrue("All the fields of KEY should be static",
                    Modifier.isStatic(modifiers));
            }

            assertTrue("All the fields of KEY should be public",
                Modifier.isPublic(modifiers));
            assertTrue("All the fields of KEY should be final",
                Modifier.isFinal(modifiers));

            try {
                if (Modifier.isStatic(modifiers)) {
                    Object o = fields[i].get(null);
                    assertTrue("All the fields of KEY should be instances of" +
                        " KEY", o instanceof KEY);
                }
            } catch (IllegalAccessException e) {
                assertTrue(false);
            }
        }
    }
}