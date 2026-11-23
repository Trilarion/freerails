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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Tests the shared deserialization filter. */
public class SerializationFilterTest extends TestCase {

    private static final SerializationFilter FILTER =
            new SerializationFilter(100, 1_000_000, 64L * 1024 * 1024, 1_000_000);

    /**
     * Tests that FreeRails, value and standard container classes are allowed.
     */
    public void testAllowsSupportedClasses() {
        assertTrue(SerializationFilter.isAllowedClass(IntPoint2D.class));
        assertTrue(SerializationFilter.isAllowedClass(String.class));
        assertTrue(SerializationFilter.isAllowedClass(ArrayList.class));
        assertTrue(SerializationFilter.isAllowedClass(int[].class));
        assertTrue(SerializationFilter.isAllowedClass(IntPoint2D[].class));
        assertTrue(SerializationFilter.isAllowedClass(List.of(1, 2).getClass()));
    }

    /**
     * Tests that unrelated JDK classes are rejected.
     */
    public void testRejectsUnsupportedClasses() {
        assertFalse(SerializationFilter.isAllowedClass(File.class));
        assertFalse(SerializationFilter.isAllowedClass(java.util.Date.class));
        assertFalse(SerializationFilter.isAllowedClass(File[].class));
    }

    /**
     * Tests that a stream containing only allowed classes is read.
     *
     * @throws Exception if the round trip fails
     */
    public void testReadsAllowedGraph() throws Exception {
        Map<String, List<IntPoint2D>> value = new HashMap<>();
        value.put("points", new ArrayList<>(List.of(new IntPoint2D(1, 2))));
        assertEquals(value, roundTrip(value, FILTER));
    }

    /**
     * Tests that a stream containing a disallowed class is rejected.
     */
    public void testRejectsDisallowedGraph() {
        List<Object> value = new ArrayList<>(List.of(new File("x")));
        TestUtils.assertThrows(InvalidClassException.class, () -> roundTrip(value, FILTER));
    }

    /**
     * Tests that the array length limit is enforced.
     */
    public void testRejectsOversizedArray() {
        SerializationFilter filter = new SerializationFilter(100, 1_000_000, 64L * 1024 * 1024, 10);
        TestUtils.assertThrows(InvalidClassException.class, () -> roundTrip(new int[11], filter));
    }

    private static Object roundTrip(Object value, ObjectInputFilter filter) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            in.setObjectInputFilter(filter);
            return in.readObject();
        }
    }
}
