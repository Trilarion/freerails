package freerails.util.ui;

import junit.framework.TestCase;

public class MyAbstractListModelTest extends TestCase {

    public void testContainsMessageWhenNoPlayersAreLoggedOn() {
        MyAbstractListModel model = new MyAbstractListModel();

        assertEquals(1, model.getSize());
        assertEquals("No players are logged on!", model.getElementAt(0));
    }

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
