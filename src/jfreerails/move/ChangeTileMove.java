/*
 * Created on Sep 9, 2004
 *
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;


/**
 * Move that changes a single tile.
 *
 * @author Luke
 *
 */
public class ChangeTileMove implements Move, MapUpdateMove {
    private final int m_x;
    private final int m_y;
    private final FreerailsTile m_before;
    private final FreerailsTile m_after;

    public ChangeTileMove(ReadOnlyWorld w, Point p, int terrainTypeAfter) {
        m_x = p.x;
        m_y = p.y;
        m_before = (FreerailsTile)w.getTile(m_x, m_y);
        m_after = FreerailsTile.getInstance(terrainTypeAfter,
                m_before.getTrackPiece());
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        FreerailsTile before = (FreerailsTile)w.getTile(m_x, m_y);
        TerrainType type = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                before.getTerrainTypeID());

        if (!type.getCategory().equals(TerrainType.Category.Country)) {
            return MoveStatus.moveFailed("Can only build on clear terrain.");
        }

        if (before.equals(m_before)) {
            return MoveStatus.MOVE_OK;
        }
		return MoveStatus.moveFailed("Expected " + m_before +
		    " but found " + before);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        FreerailsTile after = (FreerailsTile)w.getTile(m_x, m_y);

        if (after.equals(m_after)) {
            return MoveStatus.MOVE_OK;
        }
		return MoveStatus.moveFailed("Expected " + m_after + " but found " +
		    after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.setTile(m_x, m_y, m_after);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.setTile(m_x, m_y, m_before);
        }

        return ms;
    }

    public /*=const*/ Rectangle getUpdatedTiles() {
        Rectangle r = new Rectangle(m_x, m_y, 1, 1);

        return r;
    }
}