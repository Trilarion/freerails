
/*
* Interface.java
*
* Created on 08 August 2001, 20:25
*/
package jfreerails.client.view;

/**
*
* @author  Luke Lindsay
*/


public interface CursorEventListener extends java.util.EventListener {

     void cursorOneTileMove( CursorEvent ce );

     void cursorJumped( CursorEvent ce );

     void cursorKeyPressed( CursorEvent ce );
}
