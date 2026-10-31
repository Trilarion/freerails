/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.util.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * This RepaintManager is intended to be used when we are using active rendering
 * to paint a top level component. Repaint requests for components whose
 * TopLevelAncestor is the component being actively rendered in the game loop
 * are ignored; repaint requests for components whose TopLevelAncestor is
 * <strong>not</strong> the component being actively rendered in the game loop
 * are processed normally. This behaviour is needed because when menus extend
 * outside the bounds of their parent window, they have a different top level
 * component to the parent window, so are not painted when paintComponents is
 * called from the game loop.
 */
public class RepaintManagerForActiveRendering extends RepaintManager {

    /**
     * The JFrame(s) that are being actively rendered in the game loop(s).
     */
    private static final Set<JFrame> activelyRenderedComponents = ConcurrentHashMap.newKeySet();
    private static final RepaintManagerForActiveRendering instance = new RepaintManagerForActiveRendering();
    private static final LongAdder numRepaintRequests = new LongAdder();
    private static final LongAdder numDirtyRequests = new LongAdder();

    /**
     * Creates the singleton repaint manager.
     */
    private RepaintManagerForActiveRendering() {
    }

    /**
     * Installs this manager as Swing's current repaint manager.
     */
    public static void setAsCurrentManager() {
        RepaintManager.setCurrentManager(instance);
    }

    /**
     * Registers a frame that is painted by the active-rendering game loop.
     *
     * @param f actively rendered frame
     */
    public static void addJFrame(JFrame f) {
        activelyRenderedComponents.add(f);
    }

    /**
     * Unregisters a frame from active rendering.
     *
     * @param f frame to remove
     */
    public static void removeJFrame(JFrame f) {
        activelyRenderedComponents.remove(f);
    }

    /**
     * Checks whether a component belongs to a top-level window outside active
     * rendering.
     *
     * @param aComponent component to inspect
     * @return {@code true} if its top-level ancestor is not actively rendered
     */
    private static boolean hasDifferentAncestor(JComponent aComponent) {
        Container topLevelAncestor = aComponent.getTopLevelAncestor();

        return null != topLevelAncestor && !activelyRenderedComponents.contains(topLevelAncestor);
    }

    /**
     * Reports that Swing double buffering is disabled for active rendering.
     *
     * @return always {@code false}
     */
    @Override
    public boolean isDoubleBufferingEnabled() {
        return false;
    }

    /**
     * Processes repaint requests only for components outside actively rendered
     * frames.
     *
     * @param c component whose region is dirty
     * @param x dirty region's x coordinate
     * @param y dirty region's y coordinate
     * @param w dirty region's width
     * @param h dirty region's height
     */
    @Override
    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        if (hasDifferentAncestor(c)) {
            super.addDirtyRegion(c, x, y, w, h);
            numDirtyRequests.increment();
        } else {
            numRepaintRequests.increment();
        }
    }

    /**
     * Processes invalidation only for components outside actively rendered
     * frames.
     *
     * @param invalidComponent component requiring validation
     */
    @Override
    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        if (hasDifferentAncestor(invalidComponent)) {
            super.addInvalidComponent(invalidComponent);
            numDirtyRequests.increment();
        } else {
            numRepaintRequests.increment();
        }
    }

    /**
     * Marks a component clean only when it is outside an actively rendered
     * frame.
     *
     * @param aComponent component to mark clean
     */
    @Override
    public void markCompletelyClean(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyClean(aComponent);
            numDirtyRequests.increment();
        } else {
            numRepaintRequests.increment();
        }
    }

    /**
     * Marks a component dirty only when it is outside an actively rendered
     * frame.
     *
     * @param aComponent component to mark dirty
     */
    @Override
    public void markCompletelyDirty(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyDirty(aComponent);
            numDirtyRequests.increment();
        } else {
            numRepaintRequests.increment();
        }
    }
}