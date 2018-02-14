/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
import java.util.Collection;
import java.util.HashSet;

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
    private static final Collection<JFrame> activelyRenderedComponents = new HashSet<>();
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
     * @param f
     */
    public static synchronized void addJFrame(JFrame f) {
        activelyRenderedComponents.add(f);
    }

    private static boolean hasDifferentAncestor(JComponent aComponent) {
        Container topLevelAncestor = aComponent.getTopLevelAncestor();

        return null != topLevelAncestor && !activelyRenderedComponents.contains(topLevelAncestor);
    }

    @Override
    public boolean isDoubleBufferingEnabled() {
        return false;
    }

    @Override
    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        if (hasDifferentAncestor(c)) {
            super.addDirtyRegion(c, x, y, w, h);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        if (hasDifferentAncestor(invalidComponent)) {
            super.addInvalidComponent(invalidComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public void markCompletelyClean(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyClean(aComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }

    @Override
    public void markCompletelyDirty(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyDirty(aComponent);
            numDirtyRequests++;
        } else {
            numRepaintRequests++;
        }
    }
}