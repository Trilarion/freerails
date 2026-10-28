package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.JPanel;
import java.awt.event.ComponentEvent;

public class JFrameMinimumSizeEnforcerTest extends TestCase {

    public void testResizedComponentIsRaisedToMinimumWidthAndHeight() {
        JPanel component = new JPanel();
        component.setSize(40, 30);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(100, component.getWidth());
        assertEquals(80, component.getHeight());
    }

    public void testResizedComponentAlreadyAboveMinimumIsUnchanged() {
        JPanel component = new JPanel();
        component.setSize(120, 90);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(120, component.getWidth());
        assertEquals(90, component.getHeight());
    }

    public void testOnlyUndersizedDimensionIsAdjusted() {
        JPanel component = new JPanel();
        component.setSize(120, 30);
        JFrameMinimumSizeEnforcer enforcer = new JFrameMinimumSizeEnforcer(100, 80);

        enforcer.componentResized(new ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED));

        assertEquals(120, component.getWidth());
        assertEquals(80, component.getHeight());
    }
}
