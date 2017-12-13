/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 18-May-2003
 *
 */
package org.railz.client.common;

import java.awt.Container;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;


/**
 * This RepaintManager is intended to be used when we are using active
 * rendering to paint a top level component. Repaint requests for components
 * whose TopLevelAncestor is the component being actively rendered in the game
 * loop are ignored;  repaint requests for components whose TopLevelAncestor is
 * <strong>not</strong> the component being actively rendered in the game loop
 * are processed normally.  This behaviour is needed because when menus extend
 * outside the bounds of their parent window, they have a different top level
 * component to the parent window, so are not painted when paintCompoments is
 * called from the game loop.
 *
 * @author Luke
 *
 */
public final class RepaintManagerForActiveRendering extends RepaintManager {
    /** The JFrame(s) that are being actively rendered in the game loop(s). */
    private static HashSet componentsBeingActivleyRenderered = new HashSet();
    private static RepaintManagerForActiveRendering instance = new RepaintManagerForActiveRendering();

    public static void setAsCurrentManager() {
        RepaintManager.setCurrentManager(instance);
    }

    private RepaintManagerForActiveRendering() {
    }

    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w,
        int h) {
        if (hasDifferentAncestor(c)) {
            super.addDirtyRegion(c, x, y, w, h);
        }
    }

    public static synchronized void addJFrame(JFrame f) {
        componentsBeingActivleyRenderered.add(f);
    }

    public synchronized void addInvalidComponent(JComponent invalidComponent) {
        if (hasDifferentAncestor(invalidComponent)) {
            super.addInvalidComponent(invalidComponent);
        }
    }

    public void markCompletelyClean(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyClean(aComponent);
        }
    }

    public void markCompletelyDirty(JComponent aComponent) {
        if (hasDifferentAncestor(aComponent)) {
            super.markCompletelyDirty(aComponent);
        }
    }

    public boolean hasDifferentAncestor(JComponent aComponent) {
        Container topLevelAncestor = aComponent.getTopLevelAncestor();

        if (null == topLevelAncestor ||
                componentsBeingActivleyRenderered.contains(topLevelAncestor)) {
            return false;
        } else {
            return true;
        }
    }
}
