/*
 * DisplayModesComboBoxModel.java
 *
 * Created on 30 August 2003, 23:39
 */
package jfreerails.client.view;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;


/**
 *  ComboBoxModel that provides access to the screen resolutions and bit depths available.
 * @author  Luke Lindsay
 */
public class DisplayModesComboBoxModels implements javax.swing.ComboBoxModel {
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                  .getDefaultScreenDevice()
                                                                                  .getDefaultConfiguration();
    private final MyDisplayMode[] modes;
    private MyDisplayMode selection;

    public DisplayModesComboBoxModels() {
        DisplayMode currentMode = defaultConfiguration.getDevice()
                                                      .getDisplayMode();
        selection = new MyDisplayMode(currentMode);

        DisplayMode[] displayModes = defaultConfiguration.getDevice()
                                                         .getDisplayModes();
        modes = new MyDisplayMode[displayModes.length];

        for (int i = 0; i < displayModes.length; i++) {
            modes[i] = new MyDisplayMode(displayModes[i]);
        }
    }

    public Object getSelectedItem() {
        return selection;
    }

    public void setSelectedItem(Object anItem) {
        selection = (MyDisplayMode)anItem;
    }

    public void addListDataListener(javax.swing.event.ListDataListener l) {
    }

    public Object getElementAt(int index) {
        return modes[index];
    }

    public int getSize() {
        return modes.length;
    }

    public void removeListDataListener(javax.swing.event.ListDataListener l) {
    }
}