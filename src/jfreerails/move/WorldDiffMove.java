package jfreerails.move;

import static jfreerails.util.ListKey.Type.Element;
import static jfreerails.util.ListKey.Type.EndPoint;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import jfreerails.util.ListKey;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Activity;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImList;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.top.WorldDiffs.LISTID;
import jfreerails.world.top.WorldImpl.ActivityAndTime;

/**
 * A move that makes a number of changes to the map and to the lists.
 * 
 * WARNING: This class currently only handles the most common cases. A
 * UnsupportedOperationException is thrown if an appropriate move cannot be
 * generated.
 * 
 * 
 * @author Luke
 */
public class WorldDiffMove implements Move, MapUpdateMove {

    public enum Cause {
        TrainArrives, Other, YearEnd
    };

    private final Cause cause;

    private static final Logger logger = Logger.getLogger(WorldDiffMove.class
            .getName());

    public static class MapDiff implements FreerailsSerializable {
        private static final long serialVersionUID = -5935670372745313360L;

        final FreerailsSerializable before, after;

        public final int x, y;

        MapDiff(FreerailsSerializable before, FreerailsSerializable after,
                ImPoint p) {
            this.after = after;
            this.before = before;
            this.x = p.x;
            this.y = p.y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MapDiff))
                return false;

            final MapDiff diff = (MapDiff) o;

            if (x != diff.x)
                return false;
            if (y != diff.y)
                return false;
            if (!after.equals(diff.after))
                return false;
            if (!before.equals(diff.before))
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
    }

    private static final long serialVersionUID = 3905245632406239544L;

    public static WorldDiffMove generate(WorldDiffs diffs, Cause cause) {
        return new WorldDiffMove(diffs.getUnderlying(), diffs, cause);
    }

    private final ImList<MapDiff> diffs;

    private final CompositeMove listChanges;

    private final int x, y, w, h;

    public WorldDiffMove(ReadOnlyWorld world, WorldDiffs worldDiffs, Cause cause)
            throws UnsupportedOperationException {
        this.cause = cause;

        Iterator<ImPoint> mit = worldDiffs.getMapDiffs();
        ArrayList<MapDiff> diffsArrayList = new ArrayList<MapDiff>();
        if (mit.hasNext()) {
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = 0;
            int maxy = 0;
            while (mit.hasNext()) {
                ImPoint p = mit.next();
                FreerailsSerializable oldTile = world.getTile(p.x, p.y);
                FreerailsSerializable newTile = worldDiffs.getTile(p.x, p.y);
                diffsArrayList.add(new MapDiff(oldTile, newTile, p));
                minx = Math.min(minx, p.x);
                miny = Math.min(miny, p.y);
                maxx = Math.max(maxx, p.x);
                maxy = Math.max(maxy, p.y);
            }

            x = minx;
            y = miny;
            w = maxx - minx;
            h = maxy - miny;
        } else {
            x = 0;
            y = 0;
            w = 0;
            h = 0;
        }
        diffs = new ImList<MapDiff>(diffsArrayList);

        List<Move> tempList = new ArrayList<Move>();
        Iterator<ListKey> lit = worldDiffs.getListDiffs();
        while (lit.hasNext()) {
            ListKey lkey = lit.next();

            WorldDiffs.LISTID listId = (LISTID) lkey.getListID();
            switch (listId) {
            case LISTS: {
                int playerId = lkey.getIndex()[0];
                FreerailsPrincipal fp = worldDiffs.getPlayer(playerId)
                        .getPrincipal();
                KEY k = KEY.getKey(lkey.getIndex()[1]);
                if (lkey.getType() == Element) {
                    Move m;
                    int elementId = lkey.getIndex()[2];

                    // Are we changing an element?
                    if (elementId < world.size(fp, k)) {
                        FreerailsSerializable before = world.get(fp, k,
                                elementId);
                        FreerailsSerializable after = worldDiffs.get(fp, k,
                                elementId);
                        m = new ChangeItemInListMove(k, elementId, before,
                                after, fp);
                    } else {

                        FreerailsSerializable element = worldDiffs.get(fp, k,
                                elementId);
                        m = new AddItemToListMove(k, elementId, element, fp);
                    }
                    tempList.add(m);

                } else {
                    assert (lkey.getType() == EndPoint);
                    Integer newSize = (Integer) worldDiffs.getDiff(lkey);
                    int oldSize = world.size(fp, k);
                    if (newSize < oldSize) {
                        throw new UnsupportedOperationException();
                    }
                }
                break;
            }

            case CURRENT_BALANCE:
                // Do nothing. The transaction moves should take care of
                // changing
                // the values of current balance.
                break;
            case BANK_ACCOUNTS: {
                int playerId = lkey.getIndex()[0];
                FreerailsPrincipal fp = worldDiffs.getPlayer(playerId)
                        .getPrincipal();
                if (lkey.getType() == Element) {
                    Move m;
                    int elementId = lkey.getIndex()[1];

                    // Are we changing an element?
                    if (elementId < world.getNumberOfTransactions(fp)) {
                        throw new UnsupportedOperationException();
                    }
                    Transaction t = worldDiffs.getTransaction(fp, elementId);
                    m = new AddTransactionMove(fp, t);
                    tempList.add(m);

                } else {
                    assert (lkey.getType() == EndPoint);
                    Integer newSize = (Integer) worldDiffs.getDiff(lkey);
                    int oldSize = world.getNumberOfTransactions(fp);
                    if (newSize < oldSize) {
                        throw new UnsupportedOperationException();
                    }
                }
                break;
            }
            case ACTIVITY_LISTS: {
                int playerId = lkey.getIndex()[0];
                FreerailsPrincipal fp = worldDiffs.getPlayer(playerId)
                        .getPrincipal();
                Object o = worldDiffs.getDiff(lkey);
                logger.fine(lkey.toString() + " --> " + o.toString());

                switch (lkey.getIndex().length) {
                case 1: {
                    assert (lkey.getType() == EndPoint);
                    // Do nothing. Adding the activities will increase the
                    // size of the list.
                    break;

                }
                case 2:
                    assert (lkey.getType() == EndPoint);
                    // Do nothing. Adding the activities will increase the
                    // size of the list.
                    break;
                case 3: {
                    Move m;
                    // Do we need to add a new active entity?
                    int entityId = lkey.getIndex()[1];
                    ActivityAndTime aat = (ActivityAndTime) worldDiffs
                            .getDiff(lkey);
                    Activity act = aat.act;
                    int activityID = lkey.getIndex()[2];
                    if (entityId >= world.getNumberOfActiveEntities(fp)
                            && 0 == activityID) {
                        logger.fine("AddActiveEntityMove: " + act
                                + " entityId=" + entityId);
                        m = new AddActiveEntityMove(act, entityId, fp);
                    } else {
                        logger.fine("NextActivityMove: " + act + " entityId="
                                + entityId);
                        m = new NextActivityMove(act, entityId, fp);
                    }
                    tempList.add(m);
                    break;
                }
                default:
                    throw new UnsupportedOperationException(listId.toString());
                }
                break;

            }
            default:
                throw new UnsupportedOperationException(listId.toString());
            }
        }

        listChanges = new CompositeMove(tempList);
    }

    private void doMove(World world, boolean undo) {
        for (int i = 0; i < diffs.size(); i++) {
            MapDiff diff = diffs.get(i);
            FreerailsSerializable tile = undo ? diff.before : diff.after;
            world.setTile(diff.x, diff.y, tile);
        }
    }

    public MoveStatus doMove(World world, FreerailsPrincipal p) {
        MoveStatus ms = tryMapChanges(world, false);
        if (!ms.ok)
            return ms;

        ms = listChanges.doMove(world, p);

        if (ms.isOk()) {
            doMove(world, false);
        }

        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorldDiffMove))
            return false;

        final WorldDiffMove mapDiffMove = (WorldDiffMove) o;

        if (h != mapDiffMove.h)
            return false;
        if (w != mapDiffMove.w)
            return false;
        if (x != mapDiffMove.x)
            return false;
        if (y != mapDiffMove.y)
            return false;
        if (!diffs.equals(mapDiffMove.diffs))
            return false;

        return true;
    }

    public Rectangle getUpdatedTiles() {
        return new Rectangle(x, y, w, h);
    }

    @Override
    public int hashCode() {
        int result;
        result = diffs.hashCode();
        result = 29 * result + x;
        result = 29 * result + y;
        result = 29 * result + w;
        result = 29 * result + h;
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal p) {

        MoveStatus ms = tryMapChanges(world, false);
        if (!ms.ok)
            return ms;

        return ms = listChanges.tryDoMove(world, p);
    }

    private MoveStatus tryMapChanges(World world, boolean undo) {
        for (int i = 0; i < diffs.size(); i++) {
            MapDiff diff = diffs.get(i);
            FreerailsSerializable actual = world.getTile(diff.x, diff.y);
            FreerailsSerializable expected = undo ? diff.after : diff.before;
            if (!actual.equals(expected)) {
                return MoveStatus.moveFailed("expected =" + expected
                        + ", actual = " + actual);
            }
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal p) {

        MoveStatus ms = tryMapChanges(world, true);
        if (!ms.ok)
            return ms;

        return ms = listChanges.tryUndoMove(world, p);
    }

    public int listDiffs() {
        return listChanges.size();
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal p) {
        MoveStatus ms = tryMapChanges(world, true);
        if (!ms.ok)
            return ms;

        ms = listChanges.undoMove(world, p);

        if (ms.isOk()) {
            doMove(world, true);
        }

        return ms;
    }

    public Cause getCause() {
        return cause;
    }

    public CompositeMove getListChanges() {
        return listChanges;
    }

    public ImList<MapDiff> getDiffs() {
        return diffs;
    }
}