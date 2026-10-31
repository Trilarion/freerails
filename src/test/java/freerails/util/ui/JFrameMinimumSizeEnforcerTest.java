package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.JPanel;
import java.awt.event.ComponentEvent;

/**
 * Tests jframe minimum size enforcer.
 */
public class JFrameMinimumSizeEnforcerTest extends TestCase {

    /**
     * Tests resized component is raised to minimum width and height behavior.
     */
    public void testResizedComponentIsRaisedToMinimumWidthAndHeight() {
        JPanel component = new JPanel();
        component.setSize(40, 30);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(100, component.getWidth());
        assertEquals(80, component.getHeight());
    }

    /**
     * Tests resized component already above minimum is unchanged behavior.
     */
    public void testResizedComponentAlreadyAboveMinimumIsUnchanged() {
        JPanel component = new JPanel();
        component.setSize(120, 90);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(120, component.getWidth());
        assertEquals(90, component.getHeight());
    }

    /**
     * Tests only undersized dimension is adjusted behavior.
     */
    public void testOnlyUndersizedDimensionIsAdjusted() {
        JPanel component = new JPanel();
        component.setSize(120, 30);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(120, component.getWidth());
        assertEquals(80, component.getHeight());
    }
}
