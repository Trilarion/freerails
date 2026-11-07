package freerails.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Tests unmodifiable iterator behavior.
 */
public class UnmodifiableIteratorTest extends TestCase {

    /**
     * Tests factory provides read only iterator behavior.
     */
    public void testFactoryProvidesReadOnlyIterator() {
        Iterator<String> iterator = UnmodifiableIterator.of(Arrays.asList("a", "b").iterator());

        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        TestUtils.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}
