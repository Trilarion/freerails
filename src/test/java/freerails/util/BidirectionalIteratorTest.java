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
import java.util.NoSuchElementException;

public class BidirectionalIteratorTest extends TestCase {

    public void testTraversesForwardsAndBackwards() {
        BidirectionalIterator<String> iterator = new BidirectionalIterator<>(Arrays.asList("first", "second", "third"));

        assertEquals("first", iterator.get());
        assertFalse(iterator.hasPrevious());
        assertTrue(iterator.hasNext());

        iterator.next();
        assertEquals("second", iterator.get());
        assertTrue(iterator.hasPrevious());

        iterator.gotoLast();
        assertEquals("third", iterator.get());
        assertFalse(iterator.hasNext());

        iterator.previous();
        assertEquals("second", iterator.get());
        iterator.previous();
        assertEquals("first", iterator.get());
        assertFalse(iterator.hasPrevious());
    }

    public void testEmptyIteratorHasNoCurrentElementOrTraversal() {
        BidirectionalIterator<String> iterator = new BidirectionalIterator<>(Collections.emptyList());

        assertNull(iterator.get());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasPrevious());
    }

    public void testNextAndPreviousThrowWhenTraversalIsUnavailable() {
        BidirectionalIterator<Integer> iterator = new BidirectionalIterator<>(Collections.singletonList(1));

        try {
            iterator.previous();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // expected
        }

        try {
            iterator.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            // expected
        }
    }

    public void testGotoLastRejectsEmptyList() {
        BidirectionalIterator<Integer> iterator = new BidirectionalIterator<>(Collections.emptyList());

        try {
            iterator.gotoLast();
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
    }
}
