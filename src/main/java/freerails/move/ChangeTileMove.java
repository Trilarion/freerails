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
package freerails.move;

import freerails.util.Vector2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;

import java.awt.*;

/**
 * Move that changes a single tile.
 */
// TODO what if there is now water on the tile, should this not destroy tracks, cities?
public class ChangeTileMove implements Move, MapUpdateMove {

    private static final long serialVersionUID = 3256726169272662320L;
    private final Vector2D p;
    private final FullTerrainTile before;
    private final FullTerrainTile after;

    /**
     * @param w
     * @param p
     * @param terrainTypeAfter
     */
    public ChangeTileMove(ReadOnlyWorld w, Vector2D p, int terrainTypeAfter) {
        this.p = p;
        before = (FullTerrainTile) w.getTile(this.p);
        after = FullTerrainTile.getInstance(terrainTypeAfter, before.getTrackPiece());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeTileMove)) return false;

        final ChangeTileMove changeTileMove = (ChangeTileMove) obj;

        if (!p.equals(changeTileMove.p)) return false;
        if (!after.equals(changeTileMove.after)) return false;
        return before.equals(changeTileMove.before);
    }

    @Override
    public int hashCode() {
        int result;
        result = p.hashCode();
        result = 29 * result + before.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        FullTerrainTile actual = (FullTerrainTile) world.getTile(p);
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
        FullTerrainTile actual = (FullTerrainTile) world.getTile(p);
        if (actual.equals(after)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + after + " but found " + actual);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryDoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.setTile(p, after);
        }

        return moveStatus;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryUndoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.setTile(p, before);
        }

        return moveStatus;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        return new Rectangle(p.x, p.y, 1, 1);
    }
}