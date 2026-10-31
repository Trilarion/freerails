package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import java.awt.Container;
import java.awt.Rectangle;

/**
 * Tests repaint manager for active rendering behavior.
 */
public class RepaintManagerForActiveRenderingTest extends TestCase {

    /**
     * Tests processes repaint requests from different top level ancestor behavior.
     */
    public void testProcessesRepaintRequestsFromDifferentTopLevelAncestor() {
        RepaintManager previousManager = RepaintManager.currentManager(new JPanel());
        RepaintManagerForActiveRendering.setAsCurrentManager();
        RepaintManager manager = RepaintManager.currentManager(new JPanel());
        ComponentWithAncestor component = new ComponentWithAncestor(new JPanel());
        component.setSize(20, 20);

        try {
            assertTrue(manager instanceof RepaintManagerForActiveRendering);
            assertFalse(manager.isDoubleBufferingEnabled());

            manager.addDirtyRegion(component, 1, 2, 5, 6);
            manager.addInvalidComponent(component);
            manager.markCompletelyDirty(component);
            manager.markCompletelyClean(component);
        } finally {
            RepaintManager.setCurrentManager(previousManager);
        }
    }

    /**
     * Tests ignores repaint requests without top level ancestor behavior.
     */
    public void testIgnoresRepaintRequestsWithoutTopLevelAncestor() {
        RepaintManager previousManager = RepaintManager.currentManager(new JPanel());
        RepaintManagerForActiveRendering.setAsCurrentManager();
        RepaintManager manager = RepaintManager.currentManager(new JPanel());
        ComponentWithAncestor component = new ComponentWithAncestor(null);

        try {
            manager.addDirtyRegion(component, 1, 2, 5, 6);
            manager.addInvalidComponent(component);
            manager.markCompletelyDirty(component);
            manager.markCompletelyClean(component);

            assertEquals(new Rectangle(), manager.getDirtyRegion(component));
        } finally {
            RepaintManager.setCurrentManager(previousManager);
        }
    }

    /**
     * Provides test support for component with ancestor.
     */
    private static final class ComponentWithAncestor extends JComponent {
        private final Container ancestor;

        /**
         * Creates a component with ancestor.
         * @param ancestor the ancestor used by this test.
         */
        private ComponentWithAncestor(Container ancestor) {
            this.ancestor = ancestor;
        }

        /**
         * Returns top level ancestor.
         * @return the result of get top level ancestor.
         */
        @Override
        public Container getTopLevelAncestor() {
            return ancestor;
        }

        /**
         * Indicates whether showing.
         * @return whether showing.
         */
        @Override
        public boolean isShowing() {
            return ancestor != null;
        }
    }
}
