/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.move.mapupdatemove;

import freerails.model.player.Player;
import freerails.move.ClientMove;
import org.jetbrains.annotations.NotNull;

/**
 * This interface tags Moves that change the track. Track moves are sent by
 * clients and are rebound to the sending player by the server.
 */
public interface TrackMove extends MapUpdateMove, ClientMove {

    /**
     * Returns this track move acting for the supplied player.
     *
     * @param player the player the move acts for
     * @return the rebound track move
     */
    @Override
    @NotNull TrackMove withPlayer(@NotNull Player player);
}
