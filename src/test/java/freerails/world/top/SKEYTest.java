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

/*
 *
 */
package freerails.world.top;

import freerails.world.SKEY;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Test.
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