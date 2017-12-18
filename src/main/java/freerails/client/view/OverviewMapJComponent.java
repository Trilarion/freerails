package freerails.client.view;

import freerails.client.renderer.BlankMapRenderer;
import freerails.client.renderer.MapRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel that displays the overview map and a rectangle showing the region of
 * the map currently displayed on the main view.
 *
 * @author Luke
 */
public class OverviewMapJComponent extends JPanel {
    private static final long serialVersionUID = 3258697585148376888L;
    private final Rectangle mainMapVisRect;
    private MapRenderer mapView = new BlankMapRenderer(0.4F);

    /**
     *
     * @param r
     */
    public OverviewMapJComponent(Rectangle r) {
        this.setPreferredSize(mapView.getMapSizeInPixels());
        mainMapVisRect = r;
    }

    /**
     *
     * @param mv
     */
    public void setup(MapRenderer mv) {
        mapView = mv;
        this.setPreferredSize(mapView.getMapSizeInPixels());
        this.setMinimumSize(this.getPreferredSize());
        this.setSize(this.getPreferredSize());

        if (null != this.getParent()) {
            this.getParent().validate();
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Rectangle r = this.getVisibleRect();
        mapView.paintRect(g2, r);
        g2.setColor(Color.WHITE);
        g2.drawRect(mainMapVisRect.x, mainMapVisRect.y, mainMapVisRect.width,
                mainMapVisRect.height);
    }

    @Override
    public Dimension getPreferredSize() {
        return mapView.getMapSizeInPixels();
    }
}