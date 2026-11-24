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

package freerails.move.generator;

import freerails.model.player.Player;
import freerails.move.ClientMove;
import org.jetbrains.annotations.NotNull;

/**
 * A move generator that a client may send to the server. Like a
 * {@link ClientMove}, it is rebound to the player of the sending connection
 * before the server generates the move; the server rejects every other
 * generator from a client.
 */
public interface ClientMoveGenerator extends MoveGenerator {

    /**
     * Returns this generator acting for the supplied player: every player
     * reference is replaced by {@code player}.
     *
     * @param player the player the generated move acts for
     * @return the rebound generator
     */
    @NotNull ClientMoveGenerator withPlayer(@NotNull Player player);
}
