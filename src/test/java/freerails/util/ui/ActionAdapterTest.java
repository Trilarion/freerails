package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Tests action adapter behavior.
 */
public class ActionAdapterTest extends TestCase {

    /**
     * Tests actions are defensively copied behavior.
     */
    public void testActionsAreDefensivelyCopied() {
        Action original = new CountingAction("run");
        Action replacement = new CountingAction("stop");
        Action[] actions = {original};
        ActionAdapter adapter = new ActionAdapter(actions);

        actions[0] = replacement;
        Action[] returnedActions = adapter.getActions();
        returnedActions[0] = replacement;

        adapter.setSelectedItem(null);
        adapter.setSelectedItem("run");

        assertSame(original, adapter.getActions()[0]);
        assertEquals(1, ((CountingAction) original).count);
        assertEquals(0, ((CountingAction) replacement).count);
    }

    /**
     * Tests duplicate names are rejected behavior.
     */
    public void testDuplicateNamesAreRejected() {
        try {
            new ActionAdapter(new Action[]{new CountingAction("same"), new CountingAction("same")});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * Tests action name changes update combo box model behavior.
     */
    public void testActionNameChangesUpdateComboBoxModel() {
        CountingAction action = new CountingAction("before");
        ActionAdapter adapter = new ActionAdapter(new Action[]{action});

        action.putValue(Action.NAME, "after");
        assertEquals("after", adapter.getSelectedItem());
        assertEquals(0, action.count);
        adapter.setSelectedItem(null);
        adapter.setSelectedItem("after");

        assertEquals("after", adapter.getElementAt(0));
        assertEquals(1, action.count);
    }

    /**
     * Provides test support for counting action.
     */
    private static final class CountingAction extends AbstractAction {
        private int count;

        /**
         * Creates a counting action.
         * @param name the name used by this test.
         */
        private CountingAction(String name) {
            super(name);
        }

        /**
         * Performs action performed.
         * @param event the event to use in the test.
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            count++;
        }
    }
}
