/*
 * Created on 18-May-2003
 *
 */
package jfreerails.client.common;

import java.awt.Container;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;


/** This RepaintManager is intended to be used when we are using active rendering to
 * paint a top level component. Repaint requests for components whose TopLevelAncestor is
 * the component being actively rendered in the game loop are ignored;  repaint
 * requests for components whose TopLevelAncestor is <strong>not</strong> the component
 * being actively rendered in the game loop are processed normally.  This behaviour is needed
 * because when menus extend outside the bounds of their parent window, they have a different top
 * level component to the parent window, so are not painted when paintCompoments is called from the
 * game loop.
 *
 * @author Luke
 *
 */
public final class RepaintManagerForActiveRendering extends RepaintManager {
    /** The JFrame(s) that are being actively rendered in the game loop(s). */
    private static HashSet componetsBEingActivleyRenderered = new HashSet();
    private static RepaintManagerForActiveRendering instance = new RepaintManagerForActiveRendering();

    public static void setAsCurrentManager() {
        RepaintManager.setCurrentManager(instance);
    }

    private RepaintManagerForActiveRendering() {
    }

    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w,
        int h) {
        if (hasDifferentAncester(c)) {
            super.addDirtyRegion(c, x, y, w, h);
        }
    }

    public static synchronized void addJFrame(JFrame f) {
        componetsBEingActivleyRenderered.add(f);
    }

    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        if (hasDifferentAncester(invalidComponent)) {
            super.addInvalidComponent(invalidComponent);
        }
    }

    public void markCompletelyClean(JComponent aComponent) {
        if (hasDifferentAncester(aComponent)) {
            super.markCompletelyClean(aComponent);
        }
    }

    public void markCompletelyDirty(JComponent aComponent) {
        if (hasDifferentAncester(aComponent)) {
            super.markCompletelyDirty(aComponent);
        }
    }

    public boolean hasDifferentAncester(JComponent aComponent) {
        Container topLevelAncestor = aComponent.getTopLevelAncestor();

        if (null == topLevelAncestor ||
                componetsBEingActivleyRenderered.contains(topLevelAncestor)) {
            return false;
        } else {
            return true;
        }
    }
}