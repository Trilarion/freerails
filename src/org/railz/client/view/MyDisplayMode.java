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
 * MyDisplayMode.java
 *
 * Created on 31 August 2003, 00:03
 */

package org.railz.client.view;

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
