/*
 * Created on Sep 9, 2004
 *
 */
package freerails.move;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.track.FreerailsTile;

import java.awt.*;

/**
 * Move that changes a single tile.
 *
 * @author Luke
 */
public class ChangeTileMove implements Move, MapUpdateMove {
    private static final long serialVersionUID = 3256726169272662320L;

    private final int x;

    private final int y;

    private final FreerailsTile before;

    private final FreerailsTile after;

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
        if (!before.equals(changeTileMove.before))
            return false;

        return true;
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

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        FreerailsTile actual = (FreerailsTile) w.getTile(x, y);
        TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, actual
                .getTerrainTypeID());

        if (!type.getCategory().equals(TerrainType.Category.Country)) {
            return MoveStatus.moveFailed("Can only build on clear terrain.");
        }

        if (actual.equals(before)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + before + " but found "
                + actual);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        FreerailsTile actual = (FreerailsTile) w.getTile(x, y);

        if (actual.equals(after)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + after + " but found "
                + actual);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.setTile(x, y, after);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.setTile(x, y, before);
        }

        return ms;
    }

    public Rectangle getUpdatedTiles() {
        Rectangle r = new Rectangle(x, y, 1, 1);

        return r;
    }
}