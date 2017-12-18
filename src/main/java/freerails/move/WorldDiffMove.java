package freerails.move;

import freerails.util.ListKey;
import freerails.world.accounts.Transaction;
import freerails.world.common.Activity;
import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImList;
import freerails.world.common.ImPoint;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.World;
import freerails.world.top.WorldDiffs;
import freerails.world.top.WorldDiffs.LISTID;
import freerails.world.top.WorldImpl.ActivityAndTime;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static freerails.util.ListKey.Type.Element;
import static freerails.util.ListKey.Type.EndPoint;

/**
 * A move that makes a number of changes to the map and to the lists.
 * <p>
 * WARNING: This class currently only handles the most common cases. A
 * UnsupportedOperationException is thrown if an appropriate move cannot be
 * generated.
 *
 * @author Luke
 */
public class WorldDiffMove implements Move, MapUpdateMove {

    private static final Logger logger = Logger.getLogger(WorldDiffMove.class
            .getName());
    private static final long serialVersionUID = 3905245632406239544L;
    private final Cause cause;
    private final ImList<MapDiff> diffs;
    private final CompositeMove listChanges;
    private final int x, y, w, h;

    /**
     *
     * @param world
     * @param worldDiffs
     * @param cause
     * @throws UnsupportedOperationException
     */
    public WorldDiffMove(ReadOnlyWorld world, WorldDiffs worldDiffs, Cause cause)
            throws UnsupportedOperationException {
        this.cause = cause;

        Iterator<ImPoint> mit = worldDiffs.getMapDiffs();
        ArrayList<MapDiff> diffsArrayList = new ArrayList<>();
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
        diffs = new ImList<>(diffsArrayList);

        List<Move> tempList = new ArrayList<>();
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
                    if (logger.isDebugEnabled()) {
                        logger.debug(lkey.toString() + " --> " + o.toString());
                    }

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
                                if (logger.isDebugEnabled()) {
                                    logger.debug("AddActiveEntityMove: " + act
                                            + " entityId=" + entityId);
                                }
                                m = new AddActiveEntityMove(act, entityId, fp);
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("NextActivityMove: " + act
                                            + " entityId=" + entityId);
                                }
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

    /**
     *
     * @param diffs
     * @param cause
     * @return
     */
    public static WorldDiffMove generate(WorldDiffs diffs, Cause cause) {
        return new WorldDiffMove(diffs.getUnderlying(), diffs, cause);
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
        return diffs.equals(mapDiffMove.diffs);
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    public Cause getCause() {
        return cause;
    }

    /**
     *
     * @return
     */
    public CompositeMove getListChanges() {
        return listChanges;
    }

    /**
     *
     * @return
     */
    public ImList<MapDiff> getDiffs() {
        return diffs;
    }

    /**
     *
     */
    public enum Cause {

        /**
         *
         */
        TrainArrives,

        /**
         *
         */
        Other,

        /**
         *
         */
        YearEnd
    }

    /**
     *
     */
    public static class MapDiff implements FreerailsSerializable {
        private static final long serialVersionUID = -5935670372745313360L;

        /**
         *
         */
        public final int x,

        /**
         *
         */
        y;
        final FreerailsSerializable before, after;

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
            return before.equals(diff.before);
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
}