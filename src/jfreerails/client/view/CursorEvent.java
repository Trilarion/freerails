/*
* CursorEvent.java
*
* Created on 08 August 2001, 20:10
*/
package jfreerails.client.view;


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

    /** If the event was triggered by a movement of
    * precisely one tile, its vector is stored in this
    * variable.
    */
    public jfreerails.world.common.OneTileMoveVector vector;

    /** The new cursor coordinate in tiles.
    */
    public java.awt.Point newPosition;

    /** The old cursor coordinate in tiles.
    */
    public java.awt.Point oldPosition;

    /** Creates new CursorEvent
    * @param obj The object that is firing the event.
    */
    public CursorEvent(Object obj) {
        super(obj);
    }
}