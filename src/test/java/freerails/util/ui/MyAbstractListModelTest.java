package freerails.util.ui;

import junit.framework.TestCase;

/**
 * Tests my abstract list model behavior.
 */
public class MyAbstractListModelTest extends TestCase {

    /**
     * Tests contains message when no players are logged on behavior.
     */
    public void testContainsMessageWhenNoPlayersAreLoggedOn() {
        MyAbstractListModel model = new MyAbstractListModel();

        assertEquals(1, model.getSize());
        assertEquals("No players are logged on!", model.getElementAt(0));
    }

    /**
     * Tests rejects index outside model behavior.
     */
    public void testRejectsIndexOutsideModel() {
        MyAbstractListModel model = new MyAbstractListModel();

        try {
            model.getElementAt(1);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException expected) {
            // expected
        }
    }
}
