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

import freerails.controller.DisplayModeWithName;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ComboBoxModel that provides access to the screen resolutions and bit depths
 * available.
 */
public class DisplayModesComboBoxModels implements ComboBoxModel {

    private final List<DisplayModeWithName> modes = new ArrayList<>();
    private DisplayModeWithName selection;

    /**
     *
     */
    public DisplayModesComboBoxModels() {
        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        java.awt.DisplayMode currentMode = defaultConfiguration.getDevice().getDisplayMode();
        selection = new DisplayModeWithName(currentMode);

        java.awt.DisplayMode[] displayModes = defaultConfiguration.getDevice().getDisplayModes();
        for (java.awt.DisplayMode displayMode : displayModes) {
            DisplayModeWithName mode = new DisplayModeWithName(displayMode);
            modes.add(mode);
        }
    }

    /**
     * Permanently removes from the list in this object any display modes with
     * width, height, or bitdepth below the specified values.
     */
    public void removeDisplayModesBelow(int width, int height, int bitDepth) {
        Iterator<DisplayModeWithName> it = modes.iterator();
        while (it.hasNext()) {
            DisplayModeWithName mode = it.next();
            java.awt.DisplayMode displayMode = mode.displayMode;
            final boolean tooNarrow = displayMode.getWidth() < width;
            final boolean tooShort = displayMode.getHeight() < height;
            /*
             * Note, displayMode.getBitDepth() may return
             * DisplayModeWithName.BIT_DEPTH_MULTI, which is -1.
             */
            final boolean tooFewColours = (displayMode.getBitDepth() < bitDepth) && (displayMode.getBitDepth() != java.awt.DisplayMode.BIT_DEPTH_MULTI);
            if (tooNarrow || tooShort || tooFewColours) {
                it.remove();
            }
        }
    }

    public Object getSelectedItem() {
        return selection;
    }

    public void setSelectedItem(Object anItem) {
        selection = (DisplayModeWithName) anItem;
    }

    public void addListDataListener(ListDataListener l) {
    }

    public DisplayModeWithName getElementAt(int index) {
        return modes.get(index);
    }

    public int getSize() {
        return modes.size();
    }

    public void removeListDataListener(ListDataListener l) {
    }
}