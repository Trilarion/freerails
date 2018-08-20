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

package freerails.move;

import freerails.move.generator.MoveGenerator;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.nove.Status;

/**
 * A MoveExecutor that executes moves on the world object passed to its
 * constructor.
 */
public class SimpleMoveExecutor implements MoveExecutor {

    private final World world;
    private final Player player;

    /**
     * @param world
     * @param player
     */
    // TODO we really need player here, have it as argument instead of playerID
    public SimpleMoveExecutor(World world, Player player) {
        this.world = world;
        this.player = player;
    }

    /**
     * @param move
     * @return
     */
    public Status doMove(Move move) {
        return move.doMove(world, player);
    }

    /**
     * @param moveGenerator
     * @return
     */
    public Status doPreMove(MoveGenerator moveGenerator) {
        Move move = moveGenerator.generate(world);
        return move.doMove(world, player);
    }

    /**
     * @param move
     * @return
     */
    public Status tryDoMove(Move move) {
        return move.tryDoMove(world, player);
    }

    /**
     * @return
     */
    public UnmodifiableWorld getWorld() {
        return world;
    }

    /**
     * @return
     */
    public Player getPlayer() {
        return player;
    }

}
