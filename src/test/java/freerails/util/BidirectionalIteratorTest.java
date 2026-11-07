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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Tests bidirectional iterator behavior.
 */
public class BidirectionalIteratorTest extends TestCase {

    /**
     * Tests forward and backward traversal around the cursor.
     */
    public void testTraversesForwardsAndBackwards() {
        BidirectionalIterator<String> iterator = new BidirectionalIterator<>(Arrays.asList("first", "second", "third"));

        Iterator<String> standardIterator = iterator;
        assertTrue(standardIterator.hasNext());
        assertEquals("first", standardIterator.next());
        assertTrue(iterator.hasPrevious());

        assertEquals("first", iterator.previous());
        assertFalse(iterator.hasPrevious());

        assertEquals("first", iterator.next());
        assertEquals("second", iterator.next());
        iterator.gotoLast();
        assertFalse(iterator.hasNext());
        assertEquals("third", iterator.previous());

        assertEquals("second", iterator.previous());
        assertEquals("first", iterator.previous());
        assertFalse(iterator.hasPrevious());
    }

    /**
     * Tests an empty iterator has no traversal behavior.
     */
    public void testEmptyIteratorHasNoCurrentElementOrTraversal() {
        BidirectionalIterator<String> iterator = new BidirectionalIterator<>(Collections.emptyList());

        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasPrevious());
    }

    /**
     * Tests next and previous throw when traversal is unavailable behavior.
     */
    public void testNextAndPreviousThrowWhenTraversalIsUnavailable() {
        BidirectionalIterator<Integer> iterator = new BidirectionalIterator<>(Collections.singletonList(1));

        TestUtils.assertThrows(NoSuchElementException.class, iterator::previous);

        assertEquals(Integer.valueOf(1), iterator.next());
        TestUtils.assertThrows(NoSuchElementException.class, iterator::next);

        assertEquals(Integer.valueOf(1), iterator.previous());
        TestUtils.assertThrows(NoSuchElementException.class, iterator::previous);
    }

    /**
     * Tests that a reversed list view is traversed in its own order.
     */
    public void testTraversesReversedListInItsOwnOrder() {
        BidirectionalIterator<Integer> iterator = new BidirectionalIterator<>(
                Arrays.asList(1, 2, 3).reversed());

        assertEquals(Integer.valueOf(3), iterator.next());
        assertEquals(Integer.valueOf(2), iterator.next());
        assertEquals(Integer.valueOf(2), iterator.previous());
        assertEquals(Integer.valueOf(2), iterator.next());
    }

    /**
     * Tests goto last rejects empty list behavior.
     */
    public void testGotoLastRejectsEmptyList() {
        BidirectionalIterator<Integer> iterator = new BidirectionalIterator<>(Collections.emptyList());

        TestUtils.assertThrows(IndexOutOfBoundsException.class, iterator::gotoLast);
    }
}
