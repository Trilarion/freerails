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
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.World;

/**
 * A MoveExecutor that executes moves on the world object passed to its
 * constructor.
 *
 */
public class SimpleMoveExecutor implements MoveExecutor {

    private final World w;

    private final FreerailsPrincipal p;

    /**
     *
     * @param world
     * @param playerID
     */
    public SimpleMoveExecutor(World world, int playerID) {
        w = world;
        Player player = w.getPlayer(playerID);
        p = player.getPrincipal();
    }

    /**
     *
     * @param m
     * @return
     */
    public MoveStatus doMove(Move m) {
        return m.doMove(w, p);
    }

    /**
     *
     * @param pm
     * @return
     */
    public MoveStatus doPreMove(PreMove pm) {
        Move m = pm.generateMove(w);
        return m.doMove(w, p);
    }

    /**
     *
     * @param m
     * @return
     */
    public MoveStatus tryDoMove(Move m) {
        return m.tryDoMove(w, p);
    }

    /**
     *
     * @return
     */
    public ReadOnlyWorld getWorld() {
        return w;
    }

    /**
     *
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return p;
    }

}
