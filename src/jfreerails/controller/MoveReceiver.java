package jfreerails.controller;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;

public interface MoveReceiver {

	MoveStatus processMove(Move Move);

}
