package jfreerails.controller;

import java.util.ArrayList;
import jfreerails.move.AddItemToListMove;
import jfreerails.move.ChangeItemInListMove;
import jfreerails.move.RemoveItemFromListMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.UndoneMove;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
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
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.remove(moveReceiver);
    }

    public void add(MoveReceiver moveReceiver) {
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

    public void removeSplitMoveReceiver(MoveReceiver moveReceiver) {
	splitMoveReceivers.remove(moveReceiver);
    }

    public void addListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.add(listener);
    }

    public void removeListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.remove(listener);
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
                AddItemToListMove mm = (AddItemToListMove)move;
                sendItemAdded(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ChangeItemInListMove) {
                ChangeItemInListMove mm = (ChangeItemInListMove)move;
                sendListUpdated(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof RemoveItemFromListMove) {
                RemoveItemFromListMove mm = (RemoveItemFromListMove)move;
                sendItemRemoved(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof UndoneMove) {
                Move m = ((UndoneMove)move).getUndoneMove();

                if (m instanceof AddItemToListMove) {
                    AddItemToListMove mm = (AddItemToListMove)m;
		    sendItemRemoved(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                } else if (m instanceof RemoveItemFromListMove) {
                    RemoveItemFromListMove mm = (RemoveItemFromListMove)m;
		    sendItemAdded(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                } else if (move instanceof ChangeItemInListMove) {
                    ChangeItemInListMove mm = (ChangeItemInListMove)move;
		    sendListUpdated(mm.getKey(), mm.getIndex(),
			    mm.getPrincipal());
                }
            }
        }
    }

    private void sendItemAdded(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemAdded(key, index, p);
        }
    }

    private void sendItemRemoved(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.itemRemoved(key, index, p);
        }
    }

    private void sendListUpdated(KEY key, int index, FreerailsPrincipal p) {
        for (int i = 0; i < listListeners.size(); i++) {
            WorldListListener l = (WorldListListener)listListeners.get(i);
            l.listUpdated(key, index, p);
        }
    }
}
