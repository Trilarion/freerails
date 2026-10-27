package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class ActionAdapterTest extends TestCase {

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

    public void testDuplicateNamesAreRejected() {
        try {
            new ActionAdapter(new Action[]{new CountingAction("same"), new CountingAction("same")});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

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

    private static final class CountingAction extends AbstractAction {
        private int count;

        private CountingAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            count++;
        }
    }
}
