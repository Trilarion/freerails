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

package freerails.client.receiver;

import freerails.move.*;
import freerails.move.mapupdatemove.MapUpdateMove;
import freerails.server.MoveReceiver;
import freerails.util.Utils;
import freerails.model.world.WorldMapListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

// TODO put that back into the client
/**
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
public class MoveChainFork implements MoveReceiver {

    private final Collection<MoveReceiver> moveReceivers = new ArrayList<>();
    private final Collection<MoveReceiver> splitMoveReceivers = new ArrayList<>();
    private final Collection<WorldMapListener> mapListeners = new ArrayList<>();
    private long lastTickTime = System.currentTimeMillis();

    /**
     * @return
     */
    public long getLastTickTime() {
        return lastTickTime;
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
    public void addMapListener(WorldMapListener listener) {
        mapListeners.add(Utils.verifyNotNull(listener));
    }

    /**
     * @param move
     */
    @Override
    public void process(Move move) {
        // all move receivers process the move
        for (MoveReceiver moveReceiver : moveReceivers) {
            moveReceiver.process(move);
        }

        // split the move and process it
        splitMove(move);
    }

    /**
     * No CompositeMove, only single moves.
     *
     * @param move
     */
    private void splitMove(Move move) {

        // CompositeMoves are splitted here
        if (move instanceof CompositeMove) {
            for (Move subMove : ((CompositeMove) move).getMoves()) {
                splitMove(subMove);
            }
        } else {

            // all split move receivers process the move
            for (MoveReceiver moveReceiver : splitMoveReceivers) {
                moveReceiver.process(move);
            }

            if (move instanceof MapUpdateMove) {
                Rectangle rectangle = ((MapUpdateMove) move).getUpdatedTiles();
                // TODO can r be 0,0,0,0
                if (rectangle.x != 0 && rectangle.y != 0 && rectangle.width != 0 && rectangle.height != 0) {
                    for (WorldMapListener listener : mapListeners) {
                        listener.tilesChanged(rectangle);
                    }
                }
            } else if (move instanceof AdvanceClockMove) {
                lastTickTime = System.currentTimeMillis();
            }
        }
    }

}