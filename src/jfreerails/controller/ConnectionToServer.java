/**
 *
 *
 *
 *
 *
 */

package jfreerails.controller;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;




/**
 *
 *
 *
 * @author lindsal
 */

public class ConnectionToServer implements MoveReceiver {


   // associations

    public MoveReceiver moveReceiver;



   // access methods for associations


    public MoveReceiver getMoveReceiver() {
        return moveReceiver;
    }
    public void setMoveReceiver(MoveReceiver moveReceiver) {
            this.moveReceiver = moveReceiver;
    }



  // operations

/**
 * Does ...
 *
 * @param Move ...
 * @return A MoveStatus with ...
 */

    public MoveStatus processMove(Move Move) {
        return null;
    }



}





