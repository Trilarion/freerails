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

package freerails.model.world;

import freerails.model.player.Player;

/** Utility methods for inspecting world player data. */
public final class WorldUtils {

    /** Prevents instantiation of this utility class. */
    private WorldUtils() {
    }

    /**
     * Checks whether a player with a case-insensitively matching name is
     * already present in the world.
     *
     * @param world world to search
     * @param playerToAdd player whose name is checked
     * @return {@code true} if a player with the same name exists
     */
    public static boolean isAlreadyASimilarPlayer(UnmodifiableWorld world, Player playerToAdd) {
        for (Player player: world.getPlayers()) {
            if (player.getName().equalsIgnoreCase(playerToAdd.getName())) {
                return true;
            }
        }
        return false;
    }
}
