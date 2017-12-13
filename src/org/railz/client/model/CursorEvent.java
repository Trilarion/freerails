/*
 * Copyright (C) Luke Lindsay
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
* CursorEvent.java
*
* Created on 08 August 2001, 20:10
*/
package org.railz.client.model;

/** The cursor on the map fires these events when the
* cursor moves or a key is pressed.  The object
* holds the cursors current and previous position, and
* if a key was presseed, a keyEvent object.
*
* @author Luke Lindsay
* @version 1.0
*/

final public class CursorEvent extends java.util.EventObject {

    
    /** If the event was triggered by a key press, this variable
    * stores the keyEvent.
    */
    public java.awt.event.KeyEvent keyEvent;

    
    /**
     * If the event was triggered by a movement of
     * precisely one tile, its vector is stored as a CompassPoints in this
     * variable.
     */
    public byte vector;

    
    /** The new cursor coordinate in tiles.
    */
    public java.awt.Point newPosition;

    
    /** The old cursor coordinate in tiles.
    */
    public java.awt.Point oldPosition;
    
    /** Creates new CursorEvent
    * @param obj The object that is firing the event.
    */
    
    public CursorEvent( Object obj ) {
        super( obj );
    }
}
