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

package freerails.network.movereceiver;

import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;
import freerails.move.listmove.ChangeItemInListMove;
import freerails.move.listmove.ListMove;
import freerails.move.listmove.RemoveItemFromListMove;
import freerails.move.mapupdatemove.MapUpdateMove;
import freerails.util.Utils;
import freerails.model.world.WorldKey;
import freerails.model.WorldListListener;
import freerails.model.WorldMapListener;
import freerails.model.player.FreerailsPrincipal;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
public class MoveChainFork implements MoveReceiver {

    private final Collection<MoveReceiver> moveReceivers = new ArrayList<>();
    private final Collection<MoveReceiver> splitMoveReceivers = new ArrayList<>();
    private final Collection<WorldListListener> listListeners = new ArrayList<>();
    private final Collection<WorldMapListener> mapListeners = new ArrayList<>();
    private long lastTickTime = System.currentTimeMillis();

    /**
     *
     */
    public MoveChainFork() {
    }

    /**
     * @return
     */
    public long getLastTickTime() {
        return lastTickTime;
    }

    /**
     * @param l
     */
    public void addMapListener(WorldMapListener l) {
        mapListeners.add(l);
    }

    /**
     * @param moveReceiver
     */
    public void addCompleteMoveReceiver(MoveReceiver moveReceiver) {
        moveReceivers.add(Utils.verifyNotNull(moveReceiver));
    }

    /**
     * @param moveReceiver
     */
    public void addSplitMoveReceiver(MoveReceiver moveReceiver) {
        splitMoveReceivers.add(Utils.verifyNotNull(moveReceiver));
    }

    /**
     * @param listener
     */
    public void addListListener(WorldListListener listener) {
        listListeners.add(Utils.verifyNotNull(listener));
    }

    /**
     * @param move
     */
    public void process(Move move) {
        for (MoveReceiver m : moveReceivers) {
            m.process(move);
        }
        splitMove(move);
    }

    private void splitMove(Move move) {
        if (move instanceof CompositeMove) {
            List<Move> moves = ((CompositeMove) move).getMoves();

            for (Move move1 : moves) {
                splitMove(move1);
            }
        } else {
            for (MoveReceiver m : splitMoveReceivers) {
                m.process(move);
            }

            if (move instanceof AddItemToListMove) {
                ListMove mm = (AddItemToListMove) move;
                sendItemAdded(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof ChangeItemInListMove) {
                ListMove mm = (ChangeItemInListMove) move;
                sendListUpdated(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof RemoveItemFromListMove) {
                ListMove mm = (RemoveItemFromListMove) move;
                sendItemRemoved(mm.getKey(), mm.getIndex(), mm.getPrincipal());
            } else if (move instanceof MapUpdateMove) {
                Rectangle r = ((MapUpdateMove) move).getUpdatedTiles();
                if (r.x != 0 && r.y != 0 && r.width != 0 && r.height != 0) {
                    // System.out.println("TilesChanged = " + r + " "
                    // + move.getClass().getCanonicalName());
                    // if (move instanceof WorldDiffMove) {
                    // WorldDiffMove wm = (WorldDiffMove) move;
                    // ImmutableList<MapDiff> diffs = wm.getDiffs();
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

    private void sendItemAdded(WorldKey worldKey, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.itemAdded(worldKey, index, p);
        }
    }

    private void sendItemRemoved(WorldKey worldKey, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.itemRemoved(worldKey, index, p);
        }
    }

    private void sendListUpdated(WorldKey worldKey, int index, FreerailsPrincipal p) {
        for (WorldListListener l : listListeners) {
            l.listUpdated(worldKey, index, p);
        }
    }
}