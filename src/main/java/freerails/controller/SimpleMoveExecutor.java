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

package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.PreMove;
import freerails.world.ReadOnlyWorld;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

/**
 * A MoveExecutor that executes moves on the world object passed to its
 * constructor.
 */
public class SimpleMoveExecutor implements MoveExecutor {

    private final World world;
    private final FreerailsPrincipal principal;

    /**
     * @param world
     * @param playerID
     */
    public SimpleMoveExecutor(World world, int playerID) {
        this.world = world;
        Player player = this.world.getPlayer(playerID);
        principal = player.getPrincipal();
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus doMove(Move move) {
        return move.doMove(world, principal);
    }

    /**
     * @param preMove
     * @return
     */
    public MoveStatus doPreMove(PreMove preMove) {
        Move move = preMove.generateMove(world);
        return move.doMove(world, principal);
    }

    /**
     * @param move
     * @return
     */
    public MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(world, principal);
    }

    /**
     * @return
     */
    public ReadOnlyWorld getWorld() {
        return world;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

}
