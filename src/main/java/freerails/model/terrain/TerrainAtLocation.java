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

package freerails.model.terrain;

import freerails.util.Vec2D;

/**
 * Associates a map location with the identifier of its terrain type.
 */
public class TerrainAtLocation {

    private final Vec2D location;
    private final int terrainType;

    /**
     * @param location map coordinate
     * @param terrainType terrain type identifier
     */
    public TerrainAtLocation(Vec2D location, int terrainType) {
        this.location = location;
        this.terrainType = terrainType;
    }

    /**
     * @return the associated map coordinate
     */
    public Vec2D getLocation() {
        return location;
    }

    /**
     * @return the terrain type identifier
     */
    public int getTerrainType() {
        return terrainType;
    }
}