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

/*
 *
 */
package freerails.move.mapupdatemove;

import freerails.model.terrain.Terrain;
import freerails.move.Status;
import freerails.util.Vector2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainCategory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Serial;

// TODO what if there is now water on the tile, should this not destroy tracks, cities?
/**
 * Move that changes a single tile.
 */
public class ChangeTileMove implements MapUpdateMove {

    @Serial
    private static final long serialVersionUID = 3256726169272662320L;
    private final Vector2D location;
    private final TerrainTile after;

    /**
     * Creates a move to replace a map tile at the specified location.
     *
     * @param after the terrain tile to place at the location
     * @param location the map location of the tile
     */
    public ChangeTileMove(@NotNull TerrainTile after, @NotNull Vector2D location) {
        this.location = location;
        this.after = after;
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeTileMove changeTileMove)) return false;

        if (!location.equals(changeTileMove.location)) return false;
        if (!after.equals(changeTileMove.after)) return false;
        return true;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        TerrainTile actual = world.getTile(location);
        Terrain type = world.getTerrain(actual.terrainTypeId());

        if (type.getCategory() != TerrainCategory.COUNTRY) {
            return Status.fail("Can only build on clear terrain.");
        }
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }
        world.setTile(location, after);
    }

    /**
     * @return the bounds of the tile updated by this move
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return new Rectangle(location.x(), location.y(), 1, 1);
    }
}