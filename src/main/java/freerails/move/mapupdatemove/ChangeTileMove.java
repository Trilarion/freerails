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
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainCategory;

import java.awt.*;

/**
 * Move that changes a single tile.
 */
// TODO what if there is now water on the tile, should this not destroy tracks, cities?
public class ChangeTileMove implements MapUpdateMove {

    private static final long serialVersionUID = 3256726169272662320L;
    private final Vec2D location;
    private final TerrainTile before;
    private final TerrainTile after;

    /**
     * @param world
     * @param location
     * @param terrainTypeAfter
     */
    public ChangeTileMove(UnmodifiableWorld world, Vec2D location, int terrainTypeAfter) {
        this.location = location;
        before = (TerrainTile) world.getTile(this.location);
        after = new TerrainTile(terrainTypeAfter, before.getTrackPiece());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeTileMove)) return false;

        final ChangeTileMove changeTileMove = (ChangeTileMove) obj;

        if (!location.equals(changeTileMove.location)) return false;
        if (!after.equals(changeTileMove.after)) return false;
        return before.equals(changeTileMove.before);
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + before.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    public Status tryDoMove(World world, Player player) {
        TerrainTile actual = world.getTile(location);
        Terrain type = world.getTerrain(actual.getTerrainTypeId());

        if (type.getCategory() != TerrainCategory.COUNTRY) {
            return Status.moveFailed("Can only build on clear terrain.");
        }

        if (actual.equals(before)) {
            return Status.OK;
        }
        return Status.moveFailed("Expected " + before + " but found " + actual);
    }

    public Status tryUndoMove(World world, Player player) {
        TerrainTile actual = world.getTile(location);
        if (actual.equals(after)) {
            return Status.OK;
        }
        return Status.moveFailed("Expected " + after + " but found " + actual);
    }

    public Status doMove(World world, Player player) {
        Status status = tryDoMove(world, player);

        if (status.succeeds()) {
            world.setTile(location, after);
        }

        return status;
    }

    public Status undoMove(World world, Player player) {
        Status status = tryUndoMove(world, player);

        if (status.succeeds()) {
            world.setTile(location, before);
        }

        return status;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        return new Rectangle(location.x, location.y, 1, 1);
    }
}