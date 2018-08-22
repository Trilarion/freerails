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
 * DisplayModesComboBoxModel.java
 *
 */
package freerails.client.view;

import freerails.util.Vec2D;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ComboBoxModel that provides access to the screen resolutions available.
 */
public class DisplayModesComboBoxModels implements ComboBoxModel {

    private final List<Vec2D> modes = new ArrayList<>();
    private Vec2D selection;

    /**
     *
     */
    public DisplayModesComboBoxModels() {
        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        java.awt.DisplayMode currentMode = defaultConfiguration.getDevice().getDisplayMode();
        selection = new Vec2D(currentMode.getWidth(), currentMode.getHeight());

        java.awt.DisplayMode[] displayModes = defaultConfiguration.getDevice().getDisplayModes();
        for (java.awt.DisplayMode displayMode : displayModes) {
            Vec2D mode = new Vec2D(displayMode.getWidth(), displayMode.getHeight());
            modes.add(mode);
        }
    }

    /**
     * Permanently removes from the list in this object any display modes with
     * width, height, or bitdepth below the specified values.
     */
    public void removeDisplayModesBelow(int width, int height, int bitDepth) {
        Iterator<Vec2D> it = modes.iterator();
        while (it.hasNext()) {
            Vec2D mode = it.next();
            final boolean tooNarrow = mode.x < width;
            final boolean tooShort = mode.y < height;
            /*
             * Note, displayMode.getBitDepth() may return
             * DisplayModeWithName.BIT_DEPTH_MULTI, which is -1.
             */
            if (tooNarrow || tooShort) {
                it.remove();
            }
        }
    }

    @Override
    public Object getSelectedItem() {
        return selection;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (Vec2D) anItem;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public Vec2D getElementAt(int index) {
        return modes.get(index);
    }

    @Override
    public int getSize() {
        return modes.size();
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}