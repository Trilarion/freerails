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
package freerails.model.world;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 */
public class PlayerKeyTest extends TestCase {

    /**
     *
     */
    public void testGetNumberOfKeys() {
        // There were 4 keys when a wrote this test, but I expect the number to increase.
        assertTrue(PlayerKey.getNumberOfKeys() >= 4);
    }

    /**
     *
     */
    public void testThatAllTheFieldsDefinedInKEYAreInstancesOFKEY() {
        Field[] fields = PlayerKey.class.getFields();

        for (Field field : fields) {
            String name = field.getName();
            int modifiers = field.getModifiers();

            if (!name.equals("shared")) {
                assertTrue("All the fields of KEY should be static", Modifier.isStatic(modifiers));
            }

            assertTrue("All the fields of KEY should be public", Modifier.isPublic(modifiers));
            assertTrue("All the fields of KEY should be final", Modifier.isFinal(modifiers));

            try {
                if (Modifier.isStatic(modifiers)) {
                    Object o = field.get(null);
                    assertTrue("All the fields of KEY should be instances of"
                            + " KEY", o instanceof PlayerKey);
                }
            } catch (IllegalAccessException e) {
                assertTrue(false);
            }
        }
    }

    /**
     *
     */
    public void testToString() {

        assertEquals("Key.toString() should return the field name", "Trains", PlayerKey.Trains.toString());
    }
}