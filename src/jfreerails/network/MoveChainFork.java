package jfreerails.network;

import java.awt.Rectangle;
import java.util.ArrayList;
import jfreerails.move.AddItemToListMove;
import jfreerails.move.ChangeItemInListMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.MapUpdateMove;
import jfreerails.move.Move;
import jfreerails.move.RemoveItemFromListMove;
import jfreerails.move.UndoMove;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.top.WorldMapListener;


/**
 *
 * A central point at which a client may register to receive moves which have
 * been committed.
 * @author Luke
 * @author rob
 */
final public class MoveChainFork implements MoveReceiver {
    private final ArrayList<MoveReceiver> moveReceivers = new ArrayList<MoveReceiver>();
    private final ArrayList<MoveReceiver> splitMoveReceivers = new ArrayList<MoveReceiver>();
    private final ArrayList<WorldListListener> listListeners = new ArrayList<WorldListListener>();
    private final ArrayList<WorldMapListener> mapListeners = new ArrayList<WorldMapListener>();

    public MoveChainFork() {
        // do nothing
    }

    public void addMapListener(WorldMapListener l) {
        mapListeners.add(l);
    }

    public void removeMapListener(WorldMapListener l) {
        mapListeners.remove(l);
    }

    public void removeCompleteMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.remove(moveReceiver);
    }

    public void addCompleteMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.add(moveReceiver);
    }

    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        splitMoveReceivers.add(moveReceiver);
    }

    public void addListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.add(listener);
    }

    public void processMove(Move move) {
        for (int i = 0; i < moveReceivers.size(); i++) {
            MoveReceiver m = moveReceivers.get(i);
            m.processMove(move);
        }

        splitMove(move);
    }

    private void splitMove(Move move) {
        if (move instanceof UndoMove) {
            UndoMove undoneMove = (UndoMove)move;
            move = undoneMove.getUndoneMove();
        }

        if (move instanceof CompositeMove) {
            Move[] moves = ((CompositeMove)move).getMoves();

            for (int i = 0; i < moves.length; i++) {
                splitMove(moves[i]);
            }
        } else {
            for (int i = 0; i < splitMoveReceivers.size(); i++) {
                MoveReceiver m = splitMoveReceivers.get(i);
                m.processMove(move);
            }

            if (move instanceof AddItemToListMove) {
                AddItemToListMove mm = (AddItemToListMove)move;
                sendItemAdded(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ChangeItemInListMove) {
                ChangeItemInListMove mm = (ChangeItemInListMove)move;
                sendListUpdated(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof RemoveItemFromListMove) {
                RemoveItemFromListMove mm = (RemoveItemFromListMove)move;
                sendItemRemoved(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof MapUpdateMove) {
                Rectangle r = ((MapUpdateMove)move).getUpdatedTiles();
                sendMapUpdated(r);
            }
        }
    }

    private void sendMapUpdated(Rectangle r) {
        for (int i = 0; i < mapListeners.size(); i++) {
            WorldMapListener l = mapListeners.get(i);
            l.tilesChanged(r);
        }
    }

    private void sendItemAdded(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = listListeners.get(i);
            l.itemAdded(key, index, p);
        }
    }

    private void sendItemRemoved(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = listListeners.get(i);
            l.itemRemoved(key, index, p);
        }
    }

    private void sendListUpdated(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = listListeners.get(i);
            l.listUpdated(key, index, p);
        }
    }
}