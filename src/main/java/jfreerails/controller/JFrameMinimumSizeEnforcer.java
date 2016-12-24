package jfreerails.controller;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Since there is no setMinimum size method on JFrame, we use an instance of
 * this class to do the job.
 * 
 * @author Luke
 * 
 */
public class JFrameMinimumSizeEnforcer implements ComponentListener {
    private final int minWidth;

    private final int minHeight;

    public JFrameMinimumSizeEnforcer(int w, int h) {
        this.minHeight = h;
        this.minWidth = w;
    }

    public void componentResized(ComponentEvent arg0) {
        Component c = arg0.getComponent();

        int width = c.getWidth();
        int height = c.getHeight();

        // we check if either the width
        // or the height are below minimum
        boolean resize = false;

        if (width < minWidth) {
            resize = true;
            width = minWidth;
        }

        if (height < minHeight) {
            resize = true;
            height = minHeight;
        }

        if (resize) {
            c.setSize(width, height);
        }
    }

    public void componentMoved(ComponentEvent arg0) {
    }

    public void componentShown(ComponentEvent arg0) {
    }

    public void componentHidden(ComponentEvent arg0) {
    }
}