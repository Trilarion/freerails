/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.network;

import freerails.move.*;
import freerails.world.common.ImList;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.WorldListListener;
import freerails.world.top.WorldMapListener;

import java.awt.*;
import java.util.ArrayList;

/**
 * A central point at which a client may register to receive moves which have
 * been committed.
 *
 */
final public class MoveChainFork implements MoveReceiver {
    private final ArrayList<MoveReceiver> moveReceivers = new ArrayList<>();

    private final ArrayList<MoveReceiver> splitMoveReceivers = new ArrayList<>();

    private final ArrayList<WorldListListener> listListeners = new ArrayList<>();

    private final ArrayList<WorldMapListener> mapListeners = new ArrayList<>();

    private long lastTickTime = System.currentTimeMillis();

    /**
     *
     */
    public MoveChainFork() {
        // do nothing
    }

    /**
     *
     * @return
     */
    public long getLastTickTime() {
        return lastTickTime;
    }

    /**
     *
     * @param l
     */
    public void addMapListener(WorldMapListener l) {
        mapListeners.add(l);
    }

    /**
     *
     * @param l
     */
    public void removeMapListener(WorldMapListener l) {
        mapListeners.remove(l);
    }

    /**
     *
     * @param moveReceiver
     */
    public void removeCompleteMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.remove(moveReceiver);
    }

    /**
     *
     * @param moveReceiver
     */
    public void addCompleteMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        moveReceivers.add(moveReceiver);
    }

    /**
     *
     * @param moveReceiver
     */
    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        if (null == moveReceiver) {
            throw new NullPointerException();
        }

        splitMoveReceivers.add(moveReceiver);
    }

    /**
     *
     * @param listener
     */
    public void addListListener(WorldListListener listener) {
        if (null == listener) {
            throw new NullPointerException();
        }

        listListeners.add(listener);
    }

    /**
     *
     * @param move
     */
    public void processMove(Move move) {
        for (MoveReceiver m : moveReceivers) {
            m.processMove(move);
        }

        splitMove(move);
    }

    private void splitMove(Move move) {
        if (move instanceof UndoMove) {
            UndoMove undoneMove = (UndoMove) move;
            move = undoneMove.getUndoneMove();
        }

        if (move instanceof CompositeMove) {
            ImList<Move> moves = ((CompositeMove) move).getMoves();

            for (int i = 0; i < moves.size(); i++) {
                splitMove(moves.get(i));
            }
        } else {
            for (MoveReceiver m : splitMoveReceivers) {
                m.processMove(move);
            }

            if (move instanceof AddItemToListMove) {
                AddItemToListMove mm = (AddItemToListMove) move;
                sendItemAdded(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ChangeItemInListMove) {
                ChangeItemInListMove mm = (ChangeItemInListMove) move;
                sendListUpdated(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof RemoveItemFromListMove) {
                RemoveItemFromListMove mm = (RemoveItemFromListMove) move;
                sendItemRemoved(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof MapUpdateMove) {
                Rectangle r = ((MapUpdateMove) move).getUpdatedTiles();
                if (r.x != 0 && r.y != 0 && r.width != 0 && r.height != 0) {
                    // System.out.println("TilesChanged = " + r + " "
                    // + move.getClass().getCanonicalName());
                    // if (move instanceof WorldDiffMove) {
                    // WorldDiffMove wm = (WorldDiffMove) move;
                    // ImList<MapDiff> diffs = wm.getDiffs();
                    // for (int i = 0; i < diffs.size(); i++) {
                    // System.out.println(" " + diffs.get(i).x + "/"
                    // + diffs.get(i).y);
                    // }
                    // }
                    sendMapUpdated(r);
                }
            } else if (move instanceof TimeTickMove) {
                lastTickTime = System.currentTimeMillis();
            }
        }
    }

    private void sendMapUpdated(Rectangle r) {
        for (WorldMapListener l : mapListeners) {
            l.tilesChanged(r);
        }
    }

    private void sendItemAdded(KEY key, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.itemAdded(key, index, p);
        }
    }

    private void sendItemRemoved(KEY key, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.itemRemoved(key, index, p);
        }
    }

    private void sendListUpdated(KEY key, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.listUpdated(key, index, p);
        }
    }
}