package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;


/** A move that makes a number of changes to the map.
 * @author Luke
  */
public class MapDiffMove implements Move, MapUpdateMove {
    private /*=mutable*/ final ArrayList points;
    private /*=mutable*/ final ArrayList before;
    private /*=mutable*/ final ArrayList after;
    private final Rectangle updateTiles;

    public MapDiffMove(ReadOnlyWorld world, WorldDifferences diffs) {
        points = new ArrayList();
        before = new ArrayList();
        after = new ArrayList();

        Iterator it = diffs.getMapDifferences();

        while (it.hasNext()) {
            Point p = (Point)it.next();
            Rectangle tile = new Rectangle(p.x - 1, p.y - 1, 3, 3);

            points.add(p);

            FreerailsTile oldTile = (FreerailsTile)world.getTile(p.x, p.y);
            before.add(oldTile);

            FreerailsTile newTile = (FreerailsTile)diffs.getTile(p.x, p.y);
            after.add(newTile);
        }

        updateTiles = new Rectangle(0, 0, world.getMapWidth(),
                world.getMapHeight());
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, before);
    }

    private MoveStatus tryMove(World w, ArrayList arrayList) {
        for (int i = 0; i < points.size(); i++) {
            Point point = (Point)points.get(i);
            FreerailsTile actual = (FreerailsTile)w.getTile(point.x, point.y);

            FreerailsTile expected = (FreerailsTile)arrayList.get(i);

            if (!actual.equals(expected)) {
                return MoveStatus.moveFailed("expected =" + expected +
                    ", actual = " + actual);
            }
        }

        return MoveStatus.MOVE_OK;
    }

    private void doMove(World w, ArrayList arrayList) {
        for (int i = 0; i < points.size(); i++) {
            Point point = (Point)points.get(i);
            FreerailsTile tile = (FreerailsTile)arrayList.get(i);
            w.setTile(point.x, point.y, tile);
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryMove(w, before);

        if (ms.isOk()) {
            doMove(w, after);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryMove(w, after);

        if (ms.isOk()) {
            doMove(w, before);
        }

        return ms;
    }

    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        if (!(arg0 instanceof MapDiffMove)) {
            return false;
        }

        MapDiffMove test = (MapDiffMove)arg0;

        for (int i = 0; i < points.size(); i++) {
            if (!points.get(i).equals(test.points.get(i))) {
                return false;
            }

            if (!before.get(i).equals(test.before.get(i))) {
                return false;
            }

            if (!after.get(i).equals(test.after.get(i))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        return points.size();
    }

    public /*=const*/ Rectangle getUpdatedTiles() {
        return updateTiles;
    }
}