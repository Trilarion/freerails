package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;

public interface UntriedMoveReceiver extends UncommittedMoveReceiver {
	public MoveStatus tryDoMove(Move move);

	public MoveStatus tryUndoMove(Move move);
}
