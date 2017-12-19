/*
 * Created on 18-May-2003
 *
 */
package freerails.client.common;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

/**
 * This RepaintManager is intended to be used when we are using active rendering
 * to paint a top level component. Repaint requests for components whose
 * TopLevelAncestor is the component being actively rendered in the game loop
 * are ignored; repaint requests for components whose TopLevelAncestor is
 * <strong>not</strong> the component being actively rendered in the game loop
 * are processed normally. This behaviour is needed because when menus extend
 * outside the bounds of their parent window, they have a different top level
 * component to the parent window, so are not painted when paintCompoments is
 * called from the game loop.
 *
 */
public final class RepaintManagerForActiveRendering extends RepaintManager {
    /**
     * The JFrame(s) that are being actively rendered in the game loop(s).
     */
    private static final HashSet<JFrame> activelyRendereredComponents = new HashSet<>();

    private static final RepaintManagerForActiveRendering instance = new RepaintManagerForActiveRendering();

    private static long numRepaintRequests = 0;
    private static long numDirtyRequests;

    private RepaintManagerForActiveRendering() {
    }

    /**
     *
     */
    public static void setAsCurrentManager() {
        RepaintManager.setCurrentManager(instance);
    }

    /**
     *
     * @param f
     */
    public static synchronized void addJFrame(JFrame f) {
        activelyRendereredComponents.add(f);
    }

    /**
     *
     * @return
     */
    public static long getNumRepaintRequests() {
        return numRepaintRequests;
    }

    /**
     *
     * @return
     */
    public static long getNumDirtyRequests() {
        return numDirtyRequests;
    }

    @Override
    public boolean isDoubleBufferingEnabled() {
        return false;
    }

    @Override
    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w,
                                            int h) {
        if (hasDifferentAncester(c)) {
            super.addDirtyRegion(c, x, y, w, h);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        if (hasDifferentAncester(invalidComponent)) {
            super.addInvalidComponent(invalidComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public void markCompletelyClean(JComponent aComponent) {
        if (hasDifferentAncester(aComponent)) {
            super.markCompletelyClean(aComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public void markCompletelyDirty(JComponent aComponent) {
        if (hasDifferentAncester(aComponent)) {
            super.markCompletelyDirty(aComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    private boolean hasDifferentAncester(JComponent aComponent) {
        Container topLevelAncestor = aComponent.getTopLevelAncestor();

        return null != topLevelAncestor
                && !activelyRendereredComponents.contains(topLevelAncestor);
    }
}