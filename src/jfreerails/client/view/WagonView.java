/*
 * WagonView.java
 *
 * Created on 22 August 2003, 19:10
 */

package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TypeID;

/** 
 * This JLabel displays a wagon or engine. It implements ListCellRenderer so
 * that a JList can be used to display a train.
 *
 * @author  Luke Lindsay
 */
public class WagonView extends JLabel implements View, ListCellRenderer {
    
    private ReadOnlyWorld w;
    
    private ViewLists vl;
    
    /** The height in pixels to display the images at */
    private int height = 40;
    
    /** Creates a new instance of WagonView */
    public WagonView() {
    }
    
    public void setup(ReadOnlyWorld w, ViewLists vl, java.awt.event.ActionListener submitButtonCallBack) {
        this.vl = vl;
        this.w = w;
    }
    
    public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        TypeID typeID = (TypeID)value;
        Image i;
        if(typeID.getKey() == KEY.ENGINE_TYPES){
           i= vl.getTrainImages().getSideOnEngineImage(typeID.getID(), height);
        }else if(typeID.getKey() == KEY.WAGON_TYPES){
           i=  vl.getTrainImages().getSideOnWagonImage(typeID.getID(), height);
        }else{
            throw new IllegalArgumentException(String.valueOf(typeID.getKey()));
        }
        this.setIcon(new ImageIcon(i));
		Dimension d = new Dimension(this.getIcon().getIconWidth(), this.getIcon().getIconHeight());
        this.setPreferredSize(d);
        return this;
    }
    
    public int getHeight(){
        return height;
    }
    
    public void setHeight(int h){
        this.height = h;
    }    
}
