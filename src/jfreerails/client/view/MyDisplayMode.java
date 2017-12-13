/*
 * MyDisplayMode.java
 *
 * Created on 31 August 2003, 00:03
 */

package jfreerails.client.view;

import java.awt.*;

/**
 *
 * @author  Luke Lindsay
 */
public class MyDisplayMode {
    
    public final DisplayMode displayMode;
    
    /** Creates a new instance of MyDisplayMode */
    public MyDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }
    
    public String toString() {
        return displayMode.getWidth()+"x"+displayMode.getHeight()+" "+displayMode.getBitDepth()+" bit "+displayMode.getRefreshRate()+"Hz";
    }
    
    public boolean equals(Object o){
        if(o instanceof MyDisplayMode){
            MyDisplayMode test = (MyDisplayMode)o;
            return test.displayMode.equals(this.displayMode);
        }else{
            return false;
        }
    }
    
}
