package jfreerails.controller;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;




/**
 * Defining operations expected of ...
 *
 *
 * @author lindsal
 */

public interface MoveReceiver {


  // associations




  // operations

/**
 * Does ...
 *
 * @param Move ...
 * @return A MoveStatus with ...
 */

    MoveStatus processMove(Move Move);

}

