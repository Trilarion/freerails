/*
 * DisplayModesComboBoxModel.java
 *
 * Created on 30 August 2003, 23:39
 */

package jfreerails.client.view;

import java.awt.*;
/**
 *
 * @author  Luke Lindsay
 */
public class DisplayModesComboBoxModels implements javax.swing.ComboBoxModel {
    
     private GraphicsConfiguration defaultConfiguration =
			GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice()
				.getDefaultConfiguration();
     
   
     MyDisplayMode[] modes;
     
     MyDisplayMode selection;
     
     public DisplayModesComboBoxModels(){
         DisplayMode currentMode = defaultConfiguration.getDevice().getDisplayMode();
         selection = new MyDisplayMode(currentMode);
         DisplayMode[]  displayModes  = defaultConfiguration.getDevice().getDisplayModes();
         modes = new MyDisplayMode[displayModes.length];
         for (int i = 0 ; i < displayModes.length ; i ++){
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
