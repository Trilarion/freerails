package freerails.util.ui;

import junit.framework.TestCase;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

public class MyGlassPanelTest extends TestCase {

    public void testCreatesTransparentPanelWithSizedContent() {
        MyGlassPanel panel = new MyGlassPanel();

        assertFalse(panel.isOpaque());
        assertTrue(panel.getLayout() instanceof GridBagLayout);
        assertEquals(1, panel.getComponentCount());

        JComponent content = (JComponent) panel.getComponent(0);
        assertEquals(new Dimension(60, 40), content.getPreferredSize());
        assertEquals(new Dimension(60, 40), content.getMinimumSize());
        assertEquals(new Dimension(60, 40), content.getMaximumSize());
        assertEquals(Color.red, content.getBackground());
    }
}
