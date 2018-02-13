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

package freerails.move;

import freerails.util.ImmutableList;
import freerails.util.ListKey;
import freerails.util.Vector2D;
import freerails.world.*;
import freerails.world.FullWorldDiffs.LISTID;
import freerails.world.FullWorld.ActivityAndTime;
import freerails.world.finances.Transaction;
import freerails.world.player.FreerailsPrincipal;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A move that makes a number of changes to the map and to the lists.
 *
 * WARNING: This class currently only handles the most common cases. A
 * UnsupportedOperationException is thrown if an appropriate move cannot be
 * generated.
 */
public class WorldDiffMove implements Move, MapUpdateMove {

    private static final Logger logger = Logger.getLogger(WorldDiffMove.class.getName());
    private static final long serialVersionUID = 3905245632406239544L;
    private final WorldDiffMoveCause cause;
    private final ImmutableList<MapDiff> diffs;
    private final CompositeMove listChanges;
    private final int x, y, w, h;

    /**
     * @param world
     * @param worldDiffs
     * @param cause
     * @throws UnsupportedOperationException
     */
    public WorldDiffMove(ReadOnlyWorld world, FullWorldDiffs worldDiffs, WorldDiffMoveCause cause) throws UnsupportedOperationException {
        this.cause = cause;

        Iterator<Vector2D> mit = worldDiffs.getMapDiffs();
        ArrayList<MapDiff> diffsArrayList = new ArrayList<>();
        if (mit.hasNext()) {
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = 0;
            int maxy = 0;
            while (mit.hasNext()) {
                Vector2D p = mit.next();
                Serializable oldTile = world.getTile(p);
                Serializable newTile = worldDiffs.getTile(p);
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
        diffs = new ImmutableList<>(diffsArrayList);

        List<Move> tempList = new ArrayList<>();
        Iterator<ListKey> lit = worldDiffs.getListDiffs();
        while (lit.hasNext()) {
            ListKey lkey = lit.next();

            FullWorldDiffs.LISTID listId = (LISTID) lkey.getListID();
            switch (listId) {
                case LISTS: {
                    int playerId = lkey.getIndex()[0];
                    FreerailsPrincipal fp = worldDiffs.getPlayer(playerId).getPrincipal();
                    KEY k = KEY.getKey(lkey.getIndex()[1]);
                    if (lkey.getType() == ListKey.Type.Element) {
                        Move m;
                        int elementId = lkey.getIndex()[2];

                        // Are we changing an element?
                        if (elementId < world.size(fp, k)) {
                            Serializable before = world.get(fp, k, elementId);
                            Serializable after = worldDiffs.get(fp, k, elementId);
                            m = new ChangeItemInListMove(k, elementId, before, after, fp);
                        } else {

                            Serializable element = worldDiffs.get(fp, k, elementId);
                            m = new AddItemToListMove(k, elementId, element, fp);
                        }
                        tempList.add(m);
                    } else {
                        assert (lkey.getType() == ListKey.Type.EndPoint);
                        Integer newSize = (Integer) worldDiffs.getDiff(lkey);
                        int oldSize = world.size(fp, k);
                        if (newSize < oldSize) {
                            throw new UnsupportedOperationException();
                        }
                    }
                    break;
                }

                case CURRENT_BALANCE:
                    // The transaction moves should take care of
                    // changing
                    // the values of current balance.
                    break;
                case BANK_ACCOUNTS: {
                    int playerId = lkey.getIndex()[0];
                    FreerailsPrincipal fp = worldDiffs.getPlayer(playerId).getPrincipal();
                    if (lkey.getType() == ListKey.Type.Element) {
                        Move move;
                        int elementId = lkey.getIndex()[1];

                        // Are we changing an element?
                        if (elementId < world.getNumberOfTransactions(fp)) {
                            throw new UnsupportedOperationException();
                        }
                        Transaction transaction = worldDiffs.getTransaction(fp, elementId);
                        move = new AddTransactionMove(fp, transaction);
                        tempList.add(move);
                    } else {
                        assert (lkey.getType() == ListKey.Type.EndPoint);
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
                    FreerailsPrincipal fp = worldDiffs.getPlayer(playerId).getPrincipal();
                    Object o = worldDiffs.getDiff(lkey);
                    logger.debug(lkey.toString() + " --> " + o.toString());

                    switch (lkey.getIndex().length) {
                        case 1: {
                            assert (lkey.getType() == ListKey.Type.EndPoint);
                            // Do nothing. Adding the activities will increase the
                            // size of the list.
                            break;
                        }
                        case 2:
                            assert (lkey.getType() == ListKey.Type.EndPoint);
                            // Do nothing. Adding the activities will increase the
                            // size of the list.
                            break;
                        case 3: {
                            Move m;
                            // Do we need to add a new active entity?
                            int entityId = lkey.getIndex()[1];
                            ActivityAndTime aat = (ActivityAndTime) worldDiffs.getDiff(lkey);
                            Activity act = aat.act;
                            int activityID = lkey.getIndex()[2];
                            if (entityId >= world.getNumberOfActiveEntities(fp) && 0 == activityID) {
                                logger.debug("AddActiveEntityMove: " + act + " entityId=" + entityId);
                                m = new AddActiveEntityMove(act, entityId, fp);
                            } else {
                                logger.debug("NextActivityMove: " + act + " entityId=" + entityId);
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
     * @param diffs
     * @param cause
     * @return
     */
    public static WorldDiffMove generate(FullWorldDiffs diffs, WorldDiffMoveCause cause) {
        return new WorldDiffMove(diffs.getUnderlying(), diffs, cause);
    }

    private void doMove(World world, boolean undo) {
        for (int i = 0; i < diffs.size(); i++) {
            MapDiff diff = diffs.get(i);
            Serializable tile = undo ? diff.getBefore() : diff.getAfter();
            world.setTile(diff.getP(), tile);
        }
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryMapChanges(world, false);
        if (!moveStatus.succeeds()) return moveStatus;

        moveStatus = listChanges.doMove(world, principal);

        if (moveStatus.succeeds()) {
            doMove(world, false);
        }

        return moveStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorldDiffMove)) return false;

        final WorldDiffMove mapDiffMove = (WorldDiffMove) obj;

        if (h != mapDiffMove.h) return false;
        if (w != mapDiffMove.w) return false;
        if (x != mapDiffMove.x) return false;
        if (y != mapDiffMove.y) return false;
        return diffs.equals(mapDiffMove.diffs);
    }

    /**
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

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {

        MoveStatus moveStatus = tryMapChanges(world, false);
        if (!moveStatus.succeeds()) return moveStatus;

        return listChanges.tryDoMove(world, principal);
    }

    private MoveStatus tryMapChanges(ReadOnlyWorld world, boolean undo) {
        for (int i = 0; i < diffs.size(); i++) {
            MapDiff diff = diffs.get(i);
            Serializable actual = world.getTile(diff.getP());
            Serializable expected = undo ? diff.getAfter() : diff.getBefore();
            if (!actual.equals(expected)) {
                return MoveStatus.moveFailed("expected =" + expected + ", actual = " + actual);
            }
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {

        MoveStatus moveStatus = tryMapChanges(world, true);
        if (!moveStatus.succeeds()) return moveStatus;

        return listChanges.tryUndoMove(world, principal);
    }

    /**
     * @return
     */
    public int listDiffs() {
        return listChanges.size();
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryMapChanges(world, true);
        if (!moveStatus.succeeds()) return moveStatus;

        moveStatus = listChanges.undoMove(world, principal);

        if (moveStatus.succeeds()) {
            doMove(world, true);
        }

        return moveStatus;
    }

    /**
     * @return
     */
    public WorldDiffMoveCause getCause() {
        return cause;
    }

    /**
     * @return
     */
    public CompositeMove getListChanges() {
        return listChanges;
    }

}