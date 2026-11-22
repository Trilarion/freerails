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

package freerails.server;

import freerails.model.world.World;
import freerails.network.LogOnCredentials;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The content of a save-game file: the game world and the log-on credentials of its players.
 *
 * @param world the game world
 * @param credentials the credentials of the players, where the list index is the player id
 */
public record SavedGame(World world, List<LogOnCredentials> credentials) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Validates that there is exactly one credential per player and that the usernames match the player names.
     */
    public SavedGame {
        Objects.requireNonNull(world, "world");
        credentials = List.copyOf(Objects.requireNonNull(credentials, "credentials"));
        if (credentials.size() != world.getPlayers().size()) {
            throw new IllegalArgumentException("Expected " + world.getPlayers().size()
                    + " credentials but got " + credentials.size());
        }
        for (int i = 0; i < credentials.size(); i++) {
            String playerName = world.getPlayer(i).getName();
            String username = credentials.get(i).username();
            if (!playerName.equals(username)) {
                throw new IllegalArgumentException("Credentials " + i + " are for " + username
                        + " but the player is " + playerName);
            }
        }
    }
}
