/*
 * DisplayModesComboBoxModel.java
 *
 * Created on 30 August 2003, 23:39
 */
package freerails.client.view;

import freerails.controller.MyDisplayMode;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * ComboBoxModel that provides access to the screen resolutions and bit depths
 * available.
 *
 * @author Luke Lindsay
 */
public class DisplayModesComboBoxModels implements javax.swing.ComboBoxModel {
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();

    private final ArrayList<MyDisplayMode> modes = new ArrayList<MyDisplayMode>();

    private MyDisplayMode selection;

    public DisplayModesComboBoxModels() {
        DisplayMode currentMode = defaultConfiguration.getDevice()
                .getDisplayMode();
        selection = new MyDisplayMode(currentMode);

        DisplayMode[] displayModes = defaultConfiguration.getDevice()
                .getDisplayModes();
        for (int i = 0; i < displayModes.length; i++) {
            MyDisplayMode mode = new MyDisplayMode(displayModes[i]);
            modes.add(mode);
        }
    }

    /**
     * Permanently removes from the list in this object any display modes with
     * width, height, or bitdepth below the specified values.
     */
    public void removeDisplayModesBelow(int width, int height, int bitdepth) {
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
            final boolean tooFewColours = (displayMode.getBitDepth() < bitdepth)
                    && (displayMode.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI);
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

    public void addListDataListener(javax.swing.event.ListDataListener l) {
    }

    public MyDisplayMode getElementAt(int index) {
        return modes.get(index);
    }

    public int getSize() {
        return modes.size();
    }

    public void removeListDataListener(javax.swing.event.ListDataListener l) {
    }
}