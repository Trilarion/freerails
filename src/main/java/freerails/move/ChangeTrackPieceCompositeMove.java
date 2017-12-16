/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */
package freerails.move;

import java.awt.Rectangle;

import freerails.controller.PathCacheController;
import freerails.world.accounts.Transaction;
import freerails.world.common.ImPoint;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.GameRules;
import freerails.world.top.ITEM;
import freerails.world.top.ItemsTransactionAggregator;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.track.FreerailsTile;
import freerails.world.track.NullTrackPiece;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackPieceImpl;
import freerails.world.track.TrackRule;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 * 
 * @author lindsal
 * 
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove
        implements TrackMove, MapUpdateMove {
    private static final long serialVersionUID = 3616443518780978743L;

    private final int x, y, w, h;

    private final FreerailsPrincipal builder;

    private ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b,
            FreerailsPrincipal fp) {
        super(a, b);
        Rectangle r = a.getUpdatedTiles().union(b.getUpdatedTiles());
        x = r.x;
        y = r.y;
        w = r.width;
        h = r.height;
        builder = fp;
    }

    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
            ImPoint from, Step direction, TrackRule ruleA, TrackRule ruleB,
            ReadOnlyWorld w, FreerailsPrincipal principal) {
        ChangeTrackPieceMove a;
        ChangeTrackPieceMove b;
        a = getBuildTrackChangeTrackPieceMove(from, direction, ruleA, w,
                principal);
        b = getBuildTrackChangeTrackPieceMove(direction
                .createRelocatedPoint(from), direction.getOpposite(), ruleB, w,
                principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
            ImPoint from, Step direction, ReadOnlyWorld w,
            FreerailsPrincipal principal) throws Exception {
        TrackMove a;
        TrackMove b;

        a = getRemoveTrackChangeTrackPieceMove(from, direction, w, principal);
        b = getRemoveTrackChangeTrackPieceMove(direction
                .createRelocatedPoint(from), direction.getOpposite(), w,
                principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    // utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
            ImPoint p, Step direction, TrackRule trackRule, ReadOnlyWorld w,
            FreerailsPrincipal principle) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int owner = getOwner(principle, w);

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = ((FreerailsTile) w.getTile(p.x, p.y))
                    .getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.add(
                        oldTrackPiece.getTrackConfiguration(), direction);
                newTrackPiece = new TrackPieceImpl(trackConfiguration,
                        oldTrackPiece.getTrackRule(), owner, oldTrackPiece
                                .getTrackTypeID());
            } else {
                newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                        trackRule, owner, findRuleID(trackRule, w));
            }
        } else {
            newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                    trackRule, owner, findRuleID(trackRule, w));
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(ImPoint p,
            Step direction, ReadOnlyWorld w, FreerailsPrincipal principal)
            throws Exception {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = ((FreerailsTile) w.getTile(p.x, p.y))
                    .getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration
                        .subtract(oldTrackPiece.getTrackConfiguration(),
                                direction);

                if (trackConfiguration != TrackConfiguration
                        .getFlatInstance("000010000")) {
                    int owner = getOwner(principal, w);
                    newTrackPiece = new TrackPieceImpl(trackConfiguration,
                            oldTrackPiece.getTrackRule(), owner, oldTrackPiece
                                    .getTrackTypeID());
                } else {
                    newTrackPiece = NullTrackPiece.getInstance();
                }
            } else {
                // There is no track to remove.
                // Fix for bug [ 948670 ] Removing non-existant track
                throw new Exception();
            }
        } else {
            newTrackPiece = NullTrackPiece.getInstance();
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        ChangeTrackPieceMove m = new ChangeTrackPieceMove(oldTrackPiece,
                newTrackPiece, p);

        // If we are removing a station, we also need to remove the station from
        // the staiton list.
        if (oldTrackPiece.getTrackRule().isStation()
                && !newTrackPiece.getTrackRule().isStation()) {
            return RemoveStationMove.getInstance(w, m, principal);
        }
        return m;
    }

    private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(
            Step direction, TrackRule trackRule, int owner, int ruleNumber) {
        TrackConfiguration simplestConfig = TrackConfiguration
                .getFlatInstance("000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(
                simplestConfig, direction);

        return new TrackPieceImpl(trackConfiguration, trackRule, owner,
                ruleNumber);
    }

    public Rectangle getUpdatedTiles() {
        return new Rectangle(x, y, w, h);
    }

    public static int getOwner(FreerailsPrincipal p, ReadOnlyWorld w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            if (w.getPlayer(i).getPrincipal().equals(p)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }

    /** Returns true if some track has been built. */
    static boolean hasAnyTrackBeenBuilt(ReadOnlyWorld world,
            FreerailsPrincipal principal) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                world, principal);
        aggregator.setCategory(Transaction.Category.TRACK);

        return aggregator.calculateQuantity() > 0;
    }

    private static boolean mustConnectToExistingTrack(ReadOnlyWorld world) {
        GameRules rules = (GameRules) world.get(ITEM.GAME_RULES);

        return rules.isMustConnect2ExistingTrack();
    }

    @Override
    protected MoveStatus compositeTest(World world, FreerailsPrincipal p) {
        if (mustConnectToExistingTrack(world)) {
            if (hasAnyTrackBeenBuilt(world, this.builder)) {
                try {
                    ChangeTrackPieceMove a = (ChangeTrackPieceMove) super
                            .getMove(0);
                    ChangeTrackPieceMove b = (ChangeTrackPieceMove) super
                            .getMove(1);
                    int ruleBeforeA = a.trackPieceBefore.getTrackTypeID();
                    int ruleBeforeB = b.trackPieceBefore.getTrackTypeID();

                    if (ruleBeforeA == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER
                            && ruleBeforeB == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                        return MoveStatus
                                .moveFailed("Must connect to existing track");
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

    public static int findRuleID(TrackRule r, ReadOnlyWorld w) {
        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            Object o = w.get(SKEY.TRACK_RULES, i);
            if (r.equals(o)) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        PathCacheController.clearTrackCache();
        return super.doMove(w, p);
    }

    @Override
    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        PathCacheController.clearTrackCache();
        return super.undoMove(w, p);
    }
}