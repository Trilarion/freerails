package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import java.awt.Container;
import java.awt.Rectangle;

public class RepaintManagerForActiveRenderingTest extends TestCase {

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

    private static final class ComponentWithAncestor extends JComponent {
        private final Container ancestor;

        private ComponentWithAncestor(Container ancestor) {
            this.ancestor = ancestor;
        }

        @Override
        public Container getTopLevelAncestor() {
            return ancestor;
        }

        @Override
        public boolean isShowing() {
            return ancestor != null;
        }
    }
}
