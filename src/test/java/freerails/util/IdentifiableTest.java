/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

public class IdentifiableTest extends TestCase {

    public void testEqualityHashingAndOrderingUseIdentifier() {
        Identifiable first = new Identifiable(3);
        Identifiable sameId = new Identifiable(3);
        Identifiable later = new Identifiable(7);

        assertEquals(first, first);
        assertEquals(first, sameId);
        assertEquals(first.hashCode(), sameId.hashCode());
        assertFalse(first.equals(later));
        assertFalse(first.equals("3"));
        assertTrue(first.compareTo(later) < 0);
        assertTrue(later.compareTo(first) > 0);
        assertEquals(0, first.compareTo(sameId));
    }

    public void testDefaultConstructorUsesDeserializationIdentifier() {
        assertEquals(-1, new Identifiable().getId());
    }

    public void testGetByIdReturnsFirstMatchOrNull() {
        Identifiable first = new Identifiable(5);
        Identifiable second = new Identifiable(5);
        Identifiable different = new Identifiable(8);

        assertSame(first, Identifiable.getById(5, Arrays.asList(first, different, second)));
        assertNull(Identifiable.getById(9, Arrays.asList(first, different)));
        assertNull(Identifiable.getById(5, Collections.emptyList()));
    }
}
