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
package freerails.move.mapupdatemove;

import freerails.model.terrain.Terrain;
import freerails.move.Status;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainCategory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

// TODO what if there is now water on the tile, should this not destroy tracks, cities?
/**
 * Move that changes a single tile.
 */
public class ChangeTileMove implements MapUpdateMove {

    private static final long serialVersionUID = 3256726169272662320L;
    private final Vec2D location;
    private final TerrainTile after;

    /**
     * @param location
     */
    public ChangeTileMove(@NotNull TerrainTile after, @NotNull Vec2D location) {
        this.location = location;
        this.after = after;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeTileMove)) return false;

        final ChangeTileMove changeTileMove = (ChangeTileMove) obj;

        if (!location.equals(changeTileMove.location)) return false;
        if (!after.equals(changeTileMove.after)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        TerrainTile actual = world.getTile(location);
        Terrain type = world.getTerrain(actual.getTerrainTypeId());

        if (type.getCategory() != TerrainCategory.COUNTRY) {
            return Status.fail("Can only build on clear terrain.");
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }
        world.setTile(location, after);
    }

    /**
     * @return
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return new Rectangle(location.x, location.y, 1, 1);
    }
}