/*
 * DisplayModesComboBoxModel.java
 *
 * Created on 30 August 2003, 23:39
 */
package jfreerails.client.view;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Iterator;

import jfreerails.client.common.MyDisplayMode;


/**
 *  ComboBoxModel that provides access to the screen resolutions and bit depths available.
 * @author  Luke Lindsay
 */
public class DisplayModesComboBoxModels implements javax.swing.ComboBoxModel {
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                  .getDefaultScreenDevice()
                                                                                  .getDefaultConfiguration();
    private final ArrayList modes = new ArrayList();
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
    /** Permanently removes from the list in this object any display modes with width, height, or bitdepth below the
     * specified values.
     */
    public void removeDisplayModesBelow(int width, int height, int bitdepth){
    	 Iterator it = modes.iterator();
    	 while (it.hasNext()) {
        	MyDisplayMode mode = (MyDisplayMode)it.next();
        	DisplayMode displayMode = mode.displayMode;
        	if(displayMode.getWidth() < width || displayMode.getHeight() < height || displayMode.getBitDepth() < bitdepth){
        		it.remove();
        	}
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
        return modes.get(index);
    }

    public int getSize() {
        return modes.size();
    }

    public void removeListDataListener(javax.swing.event.ListDataListener l) {
    }
}