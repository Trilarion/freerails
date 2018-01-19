package freerails.client.common;

import freerails.move.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.UntriedMoveReceiver;

class MyUntriedMoveReceiver implements UntriedMoveReceiver {

    public void process(Move move) {
    }

    public void processPreMove(PreMove preMove) {
    }

    public MoveStatus tryDoMove(Move move) {
        return MoveStatus.moveFailed("No move receiver set on model root!");
    }
}
