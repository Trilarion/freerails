package jfreerails.controller;

import java.util.ArrayList;
import jfreerails.move.AddItemToListMove;
import jfreerails.move.ChangeItemInListMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.world.top.WorldListListener;


/**
 * @version         1.0
 *
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
final public class MoveChainFork implements MoveReceiver {
    private final ArrayList moveReceivers = new ArrayList();
    private final ArrayList splitMoveReceivers = new ArrayList();
    private final ArrayList listListeners = new ArrayList();

    public MoveChainFork() {
        // do nothing
    }

    public void remove(MoveReceiver moveReceiver) {
        moveReceivers.remove(moveReceiver);
    }

    public void add(MoveReceiver moveReceiver) {
        moveReceivers.add(moveReceiver);
    }

    public void removeSplitMoveReceiver(MoveReceiver moveReceiver) {
        splitMoveReceivers.remove(moveReceiver);
    }

    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        splitMoveReceivers.add(moveReceiver);
    }

    public void removeListListener(WorldListListener listener) {
        listListeners.remove(listener);
    }

    public void addListListener(WorldListListener listener) {
        listListeners.add(listener);
    }

    /*
     * @see MoveReceiver#processMove(Move)
     */
    public void processMove(Move move) {
        for (int i = 0; i < moveReceivers.size(); i++) {
            MoveReceiver m = (MoveReceiver)moveReceivers.get(i);
            m.processMove(move);
        }
		splitMove(move);
    }

    private void splitMove(Move move) {
        if (move instanceof CompositeMove) {
            Move[] moves = ((CompositeMove)move).getMoves();

            for (int i = 0; i < moves.length; i++) {
                splitMove(moves[i]);
            }
        } else {
            for (int i = 0; i < splitMoveReceivers.size(); i++) {
                MoveReceiver m = (MoveReceiver)splitMoveReceivers.get(i);
                m.processMove(move);
            }

            if (move instanceof AddItemToListMove) {
                for (int i = 0; i < listListeners.size(); i++) {
                    AddItemToListMove mm = (AddItemToListMove)move;
                    WorldListListener l = (WorldListListener)listListeners.get(i);
                    l.itemAdded(mm.getKey(), mm.getIndex());
                }
            } else if (move instanceof ChangeItemInListMove) {
                for (int i = 0; i < listListeners.size(); i++) {
                    ChangeItemInListMove mm = (ChangeItemInListMove)move;
                    WorldListListener l = (WorldListListener)listListeners.get(i);
                    l.listUpdated(mm.getKey(), mm.getIndex());
                }
            }
        }
    }
}