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
package freerails.client.view;

import java.awt.*;

/**
 * Stores a list of colours to use to represent different players.
 */
class PlayerColors {

    private static final Color[] colors = new Color[]{Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.YELLOW};// Save

    private PlayerColors() {
    }

    // keep red for when we need to grab the player's attention!

    /**
     * @param playerNumber
     * @return
     */

    public static Color getColor(int playerNumber) {
        return colors[playerNumber % colors.length];
    }

}
