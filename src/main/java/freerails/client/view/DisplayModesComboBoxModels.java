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

import freerails.controller.MyDisplayMode;

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

    private final List<MyDisplayMode> modes = new ArrayList<>();
    private MyDisplayMode selection;

    /**
     *
     */
    public DisplayModesComboBoxModels() {
        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        DisplayMode currentMode = defaultConfiguration.getDevice().getDisplayMode();
        selection = new MyDisplayMode(currentMode);

        DisplayMode[] displayModes = defaultConfiguration.getDevice().getDisplayModes();
        for (DisplayMode displayMode : displayModes) {
            MyDisplayMode mode = new MyDisplayMode(displayMode);
            modes.add(mode);
        }
    }

    /**
     * Permanently removes from the list in this object any display modes with
     * width, height, or bitdepth below the specified values.
     */
    public void removeDisplayModesBelow(int width, int height, int bitDepth) {
        Iterator<MyDisplayMode> it = modes.iterator();
        while (it.hasNext()) {
            MyDisplayMode mode = it.next();
            DisplayMode displayMode = mode.displayMode;
            final boolean tooNarrow = displayMode.getWidth() < width;
            final boolean tooShort = displayMode.getHeight() < height;
            /*
             * Note, displayMode.getBitDepth() may return
             * DisplayMode.BIT_DEPTH_MULTI, which is -1.
             */
            final boolean tooFewColours = (displayMode.getBitDepth() < bitDepth) && (displayMode.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI);
            if (tooNarrow || tooShort || tooFewColours) {
                it.remove();
            }
        }

    }

    public Object getSelectedItem() {
        return selection;
    }

    public void setSelectedItem(Object anItem) {
        selection = (MyDisplayMode) anItem;
    }

    public void addListDataListener(ListDataListener l) {
    }

    public MyDisplayMode getElementAt(int index) {
        return modes.get(index);
    }

    public int getSize() {
        return modes.size();
    }

    public void removeListDataListener(ListDataListener l) {
    }
}