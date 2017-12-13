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
 * ProgressMonitor.java
 *
 * Created on 08 September 2003, 21:56
 */
package org.railz.util;


/** This interface defines callbacks that can be used to let the user know how a slow task is progressing.
 *
 * @author  Luke Lindsay
 */
public interface FreerailsProgressMonitor {
    public static final FreerailsProgressMonitor NULL_INSTANCE = new FreerailsProgressMonitor() {
            public void setMessage(String s) {
            }

            public void setValue(int i) {
            }

            public void setMax(int max) {
            }
        };

    void setMessage(String s);

    void setValue(int i);

    void setMax(int max);
}
