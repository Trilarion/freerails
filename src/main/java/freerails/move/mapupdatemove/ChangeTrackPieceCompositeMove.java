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
package freerails.move.mapupdatemove;

import freerails.model.world.*;
import freerails.move.*;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.util.Vec2D;
import freerails.model.finances.ItemsTransactionAggregator;
import freerails.model.finances.TransactionCategory;
import freerails.model.game.GameRules;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;

import java.awt.*;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove implements TrackMove {

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
     * @param world
     * @param principal
     * @return
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(Vec2D from, TileTransition direction, TrackRule ruleA, TrackType typeA, TrackRule ruleB, TrackType typeB, UnmodifiableWorld world, FreerailsPrincipal principal) {
        ChangeTrackPieceMove a = getBuildTrackChangeTrackPieceMove(from, direction, ruleA, typeA, world, principal);
        ChangeTrackPieceMove b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), ruleB, typeB, world, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    /**
     * @param from
     * @param direction
     * @param world
     * @param principal
     * @return
     * @throws Exception
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(Vec2D from, TileTransition direction, UnmodifiableWorld world, FreerailsPrincipal principal) throws Exception {
        TrackMove a = getRemoveTrackChangeTrackPieceMove(from, direction, world, principal);
        TrackMove b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), world, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    // utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, TrackRule trackRule, TrackType trackType, UnmodifiableWorld world, FreerailsPrincipal principal) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int owner = WorldUtils.getPlayerIndex(world, principal);

        if (world.boundsContain(p)) {
            oldTrackPiece = ((TerrainTile) world.getTile(p)).getTrackPiece();

            if (oldTrackPiece != null) {
                TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(), direction);
                newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackRule(), oldTrackPiece.getTrackType(), owner, oldTrackPiece.getTrackTypeID());
            } else {
                newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule, trackType, owner, findRuleID(trackRule, world));
            }
        } else {
            newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule, trackType, owner, findRuleID(trackRule, world));
            oldTrackPiece = null;
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, UnmodifiableWorld world, FreerailsPrincipal principal) throws Exception {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (world.boundsContain(p)) {
            oldTrackPiece = ((TerrainTile) world.getTile(p)).getTrackPiece();

            if (oldTrackPiece != null) {
                TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(), direction);

                if (trackConfiguration != TrackConfiguration.getFlatInstance("000010000")) {
                    int owner = WorldUtils.getPlayerIndex(world, principal);
                    newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackRule(), oldTrackPiece.getTrackType(), owner, oldTrackPiece.getTrackTypeID());
                } else {
                    newTrackPiece = null;
                }
            } else {
                // There is no track to remove.
                // Fix for bug [ 948670 ] Removing non-existent track
                throw new Exception();
            }
        } else {
            newTrackPiece = null;
            oldTrackPiece = null;
        }

        ChangeTrackPieceMove changeTrackPieceMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);

        // If we are removing a station, we also need to remove the station from
        // the station list.
        if (oldTrackPiece.getTrackType().isStation() && !newTrackPiece.getTrackType().isStation()) {
            return RemoveStationMove.getInstance(world, changeTrackPieceMove, principal);
        }
        return changeTrackPieceMove;
    }

    private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(TrackConfigurations direction, TrackRule trackRule, TrackType trackType, int owner, int ruleNumber) {
        TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig, direction);

        return new TrackPiece(trackConfiguration, trackRule, trackType, owner, ruleNumber);
    }

    /**
     * Returns true if some track has been built.
     */
    public static boolean hasAnyTrackBeenBuilt(UnmodifiableWorld world, FreerailsPrincipal principal) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);

        return aggregator.calculateQuantity() > 0;
    }

    /**
     * @param rule
     * @param world
     * @return
     */
    private static int findRuleID(TrackRule rule, UnmodifiableWorld world) {
        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            Object o = world.get(SharedKey.TrackRules, i);
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
    public MoveStatus compositeTest(World world) {
        // must connect to existing track
        GameRules rules = (GameRules) world.get(WorldItem.GameRules);

        if (rules.mustConnectToExistingTrack()) {
            if (hasAnyTrackBeenBuilt(world, builder)) {
                try {
                    ChangeTrackPieceMove a = (ChangeTrackPieceMove) super.getMove(0);
                    ChangeTrackPieceMove b = (ChangeTrackPieceMove) super.getMove(1);

                    if (a.trackPieceBefore == null && b.trackPieceBefore == null) {
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
        MoveTrainMoveGenerator.clearCache();
        return super.doMove(world, principal);
    }

    @Override
    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveTrainMoveGenerator.clearCache();
        return super.undoMove(world, principal);
    }
}