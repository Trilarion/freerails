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

/*
 *
 */
package freerails.network;

import freerails.move.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.UntriedMoveReceiver;
import freerails.util.Utils;
import freerails.world.World;
import freerails.world.player.Player;

/**
 * An UntriedMoveReceiver that executes moves on the world object passed to its
 * constructor.
 */
public final class SimpleMoveReciever implements UntriedMoveReceiver {

    private final World world;

    /**
     * @param world
     */
    public SimpleMoveReciever(World world) {
        this.world = Utils.verifyNotNull(world);
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(world, Player.AUTHORITATIVE);
    }

    /**
     * @param move
     */
    public void process(Move move) {
        move.doMove(world, Player.AUTHORITATIVE);
    }

    /**
     * @param preMove
     */
    public void processPreMove(PreMove preMove) {
        process(preMove.generateMove(world));
    }
}