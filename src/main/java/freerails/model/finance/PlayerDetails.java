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

package freerails.model.finance;

import org.jetbrains.annotations.NotNull;

/**
 * Stores the details a player that are shown on the leader board.
 */
public class PlayerDetails implements Comparable<PlayerDetails> {

    // TODO set in constructor?
    private String name = "player";
    private Money networth = Money.ZERO;
    private int stations = 0;

    @Override
    public String toString() {
        return name + ", " + networth.toString() + " net worth, " + stations + "  stations.";
    }

    @Override
    public int compareTo(@NotNull PlayerDetails o) {
        return networth.compareTo(o.networth);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getNetworth() {
        return networth;
    }

    public void setNetworth(Money networth) {
        this.networth = networth;
    }

    public int getStations() {
        return stations;
    }

    public void setStations(int stations) {
        this.stations = stations;
    }
}
