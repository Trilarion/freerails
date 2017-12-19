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
 * Created on Sep 9, 2004
 *
 */
package freerails.move;

import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FreerailsTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;

import java.awt.*;

/**
 * Move that changes a single tile.
 */
// TODO what if there is now water on the tile, should this not destroy tracks, cities?
public class ChangeTileMove implements Move, MapUpdateMove {
    private static final long serialVersionUID = 3256726169272662320L;
    private final int x;
    private final int y;
    private final FreerailsTile before;
    private final FreerailsTile after;

    /**
     * @param w
     * @param p
     * @param terrainTypeAfter
     */
    public ChangeTileMove(ReadOnlyWorld w, Point p, int terrainTypeAfter) {
        this.x = p.x;
        this.y = p.y;
        this.before = (FreerailsTile) w.getTile(x, y);
        this.after = FreerailsTile.getInstance(terrainTypeAfter, before
                .getTrackPiece());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeTileMove))
            return false;

        final ChangeTileMove changeTileMove = (ChangeTileMove) o;

        if (x != changeTileMove.x)
            return false;
        if (y != changeTileMove.y)
            return false;
        if (!after.equals(changeTileMove.after))
            return false;
        return before.equals(changeTileMove.before);
    }

    @Override
    public int hashCode() {
        int result;
        result = x;
        result = 29 * result + y;
        result = 29 * result + before.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        FreerailsTile actual = (FreerailsTile) world.getTile(x, y);
        TerrainType type = (TerrainType) world.get(SKEY.TERRAIN_TYPES, actual
                .getTerrainTypeID());

        if (!type.getCategory().equals(TerrainCategory.Country)) {
            return MoveStatus.moveFailed("Can only build on clear terrain.");
        }

        if (actual.equals(before)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + before + " but found "
                + actual);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        FreerailsTile actual = (FreerailsTile) world.getTile(x, y);

        if (actual.equals(after)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + after + " but found "
                + actual);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryDoMove(world, principal);

        if (ms.isOk()) {
            world.setTile(x, y, after);
        }

        return ms;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryUndoMove(world, principal);

        if (ms.isOk()) {
            world.setTile(x, y, before);
        }

        return ms;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        return new Rectangle(x, y, 1, 1);
    }
}