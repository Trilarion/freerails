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
 * ChangeTrackPieceCompositeMove.java
 *
 */
package freerails.move;

import freerails.util.Vector2D;
import freerails.world.*;
import freerails.world.finances.TransactionCategory;
import freerails.world.game.GameRules;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.track.*;

import java.awt.*;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove implements TrackMove, MapUpdateMove {
    private static final long serialVersionUID = 3616443518780978743L;
    private final int x, y, w, h;
    private final FreerailsPrincipal builder;

    private ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b, FreerailsPrincipal fp) {
        super(a, b);
        Rectangle r = a.getUpdatedTiles().union(b.getUpdatedTiles());
        x = r.x;
        y = r.y;
        w = r.width;
        h = r.height;
        builder = fp;
    }

    /**
     * @param from
     * @param direction
     * @param ruleA
     * @param ruleB
     * @param w
     * @param principal
     * @return
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(Vector2D from, TileTransition direction, TrackRule ruleA, TrackRule ruleB, ReadOnlyWorld w, FreerailsPrincipal principal) {
        ChangeTrackPieceMove a;
        ChangeTrackPieceMove b;
        a = getBuildTrackChangeTrackPieceMove(from, direction, ruleA, w, principal);
        b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), ruleB, w, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    /**
     * @param from
     * @param direction
     * @param w
     * @param principal
     * @return
     * @throws Exception
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(Vector2D from, TileTransition direction, ReadOnlyWorld w, FreerailsPrincipal principal) throws Exception {
        TrackMove a;
        TrackMove b;

        a = getRemoveTrackChangeTrackPieceMove(from, direction, w, principal);
        b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), w, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    // utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(Vector2D p, TrackConfigurations direction, TrackRule trackRule, ReadOnlyWorld w, FreerailsPrincipal principle) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int owner = getOwner(principle, w);

        if (w.boundsContain(p)) {
            oldTrackPiece = ((FullTerrainTile) w.getTile(p)).getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(), direction);
                newTrackPiece = new TrackPieceImpl(trackConfiguration, oldTrackPiece.getTrackRule(), owner, oldTrackPiece.getTrackTypeID());
            } else {
                newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule, owner, findRuleID(trackRule, w));
            }
        } else {
            newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule, owner, findRuleID(trackRule, w));
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Vector2D p, TrackConfigurations direction, ReadOnlyWorld w, FreerailsPrincipal principal) throws Exception {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (w.boundsContain(p)) {
            oldTrackPiece = ((FullTerrainTile) w.getTile(p)).getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(), direction);

                if (trackConfiguration != TrackConfiguration.getFlatInstance("000010000")) {
                    int owner = getOwner(principal, w);
                    newTrackPiece = new TrackPieceImpl(trackConfiguration, oldTrackPiece.getTrackRule(), owner, oldTrackPiece.getTrackTypeID());
                } else {
                    newTrackPiece = NullTrackPiece.getInstance();
                }
            } else {
                // There is no track to remove.
                // Fix for bug [ 948670 ] Removing non-existent track
                throw new Exception();
            }
        } else {
            newTrackPiece = NullTrackPiece.getInstance();
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        ChangeTrackPieceMove changeTrackPieceMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);

        // If we are removing a station, we also need to remove the station from
        // the station list.
        if (oldTrackPiece.getTrackRule().isStation() && !newTrackPiece.getTrackRule().isStation()) {
            return RemoveStationMove.getInstance(w, changeTrackPieceMove, principal);
        }
        return changeTrackPieceMove;
    }

    private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(TrackConfigurations direction, TrackRule trackRule, int owner, int ruleNumber) {
        TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig, direction);

        return new TrackPieceImpl(trackConfiguration, trackRule, owner, ruleNumber);
    }

    /**
     * @param p
     * @param world
     * @return
     */
    public static int getOwner(FreerailsPrincipal p, ReadOnlyWorld world) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            if (world.getPlayer(i).getPrincipal().equals(p)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Returns true if some track has been built.
     */
    static boolean hasAnyTrackBeenBuilt(ReadOnlyWorld world, FreerailsPrincipal principal) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);

        return aggregator.calculateQuantity() > 0;
    }

    private static boolean mustConnectToExistingTrack(ReadOnlyWorld world) {
        GameRules rules = (GameRules) world.get(ITEM.GAME_RULES);

        return rules.isMustConnectToExistingTrack();
    }

    /**
     * @param rule
     * @param world
     * @return
     */
    private static int findRuleID(TrackRule rule, ReadOnlyWorld world) {
        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            Object o = world.get(SKEY.TRACK_RULES, i);
            if (rule.equals(o)) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {
        return new Rectangle(x, y, w, h);
    }

    @Override
    protected MoveStatus compositeTest(World w) {
        if (mustConnectToExistingTrack(w)) {
            if (hasAnyTrackBeenBuilt(w, builder)) {
                try {
                    ChangeTrackPieceMove a = (ChangeTrackPieceMove) super.getMove(0);
                    ChangeTrackPieceMove b = (ChangeTrackPieceMove) super.getMove(1);
                    int ruleBeforeA = a.trackPieceBefore.getTrackTypeID();
                    int ruleBeforeB = b.trackPieceBefore.getTrackTypeID();

                    if (ruleBeforeA == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER && ruleBeforeB == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                        return MoveStatus.moveFailed("Must connect to existing track");
                    }
                } catch (ClassCastException e) {
                    // It was not the type of move we expected.
                    // We end up here when we are removing a station.
                    return MoveStatus.MOVE_OK;
                }
            }
        }

        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveTrainPreMove.clearCache();
        return super.doMove(world, principal);
    }

    @Override
    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveTrainPreMove.clearCache();
        return super.undoMove(world, principal);
    }
}