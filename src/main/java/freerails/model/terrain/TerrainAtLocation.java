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
public record TerrainAtLocation(Vec2D location, int terrainType) {

    /**
     * @param location    map coordinate
     * @param terrainType terrain type identifier
     */
    public TerrainAtLocation {
    }

    /**
     * @return the associated map coordinate
     */
    @Override
    public Vec2D location() {
        return location;
    }

    /**
     * @return the terrain type identifier
     */
    @Override
    public int terrainType() {
        return terrainType;
    }
}