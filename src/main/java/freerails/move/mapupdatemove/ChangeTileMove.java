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

import freerails.move.MoveStatus;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.world.World;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainType;

import java.awt.*;

/**
 * Move that changes a single tile.
 */
// TODO what if there is now water on the tile, should this not destroy tracks, cities?
public class ChangeTileMove implements MapUpdateMove {

    private static final long serialVersionUID = 3256726169272662320L;
    private final Vector2D location;
    private final FullTerrainTile before;
    private final FullTerrainTile after;

    /**
     * @param world
     * @param location
     * @param terrainTypeAfter
     */
    public ChangeTileMove(ReadOnlyWorld world, Vector2D location, int terrainTypeAfter) {
        this.location = location;
        before = (FullTerrainTile) world.getTile(this.location);
        after = FullTerrainTile.getInstance(terrainTypeAfter, before.getTrackPiece());
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

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        FullTerrainTile actual = (FullTerrainTile) world.getTile(location);
        TerrainType type = (TerrainType) world.get(SKEY.TERRAIN_TYPES, actual.getTerrainTypeID());

        if (type.getCategory() != TerrainCategory.Country) {
            return MoveStatus.moveFailed("Can only build on clear terrain.");
        }

        if (actual.equals(before)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + before + " but found " + actual);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        FullTerrainTile actual = (FullTerrainTile) world.getTile(location);
        if (actual.equals(after)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + after + " but found " + actual);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryDoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.setTile(location, after);
        }

        return moveStatus;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryUndoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.setTile(location, before);
        }

        return moveStatus;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        return new Rectangle(location.x, location.y, 1, 1);
    }
}