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

package freerails.move;

import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A move that a client may send to the server.
 *
 * <p>The server does not trust the player identity in a client's message. Before it
 * checks or applies a client move, it replaces every player reference in the move,
 * including in contained moves, with the player of the connection the move arrived
 * on (see {@link #withPlayer(Player)}). The server rejects every move from a client
 * that is not a {@code ClientMove}.
 */
public interface ClientMove extends Move {

    /**
     * Returns this move acting for the supplied player: every player reference,
     * including those of contained moves, is replaced by {@code player}.
     *
     * @param player the player the move acts for
     * @return the rebound move, or this move if it holds no player reference
     * @throws IllegalArgumentException if a contained move may not be sent by a client
     */
    @NotNull ClientMove withPlayer(@NotNull Player player);

    /**
     * Returns the supplied move acting for the supplied player.
     *
     * @param move a move that may be sent by a client
     * @param player the player the move acts for
     * @return the rebound move
     * @throws IllegalArgumentException if {@code move} may not be sent by a client
     */
    static @NotNull ClientMove withPlayer(@NotNull Move move, @NotNull Player player) {
        if (!(move instanceof ClientMove clientMove)) {
            throw new IllegalArgumentException("A client may not send " + move.getClass().getSimpleName() + ".");
        }
        return clientMove.withPlayer(player);
    }
}
