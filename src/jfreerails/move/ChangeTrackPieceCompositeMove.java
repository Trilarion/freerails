/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 * @author  lindsal
 *
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove
    implements TrackMove, MapUpdateMove {
    private final Rectangle updatedTiles;
    private final FreerailsPrincipal builder;

    /** Creates new ChangeTrackPieceCompositeMove */
    private ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b,
        FreerailsPrincipal fp) {
        super(new Move[] {a, b});
        updatedTiles = a.getUpdatedTiles().union(b.getUpdatedTiles());
        builder = fp;
    }

    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
        Point from, OneTileMoveVector direction, TrackRule trackRule,
        ReadOnlyWorld w, FreerailsPrincipal principal) {
        ChangeTrackPieceMove a;
        ChangeTrackPieceMove b;
        a = getBuildTrackChangeTrackPieceMove(from, direction, trackRule, w,
                principal);
        b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(
                    from), direction.getOpposite(), trackRule, w, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
        Point from, OneTileMoveVector direction, ReadOnlyWorld w,
        FreerailsPrincipal principal) {
        TrackMove a;
        TrackMove b;

        a = getRemoveTrackChangeTrackPieceMove(from, direction, w, principal);
        b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(
                    from), direction.getOpposite(), w, principal);

        return new ChangeTrackPieceCompositeMove(a, b, principal);
    }

    //utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
        Point p, OneTileMoveVector direction, TrackRule trackRule,
        ReadOnlyWorld w, FreerailsPrincipal principle) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int owner = getOwner(principle, w);

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = ((FreerailsTile)w.getTile(p.x, p.y)).getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(),
                        direction);
                newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration,
                        owner);
            } else {
                newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                        trackRule, owner);
            }
        } else {
            newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                    trackRule, owner);
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    //utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Point p,
        OneTileMoveVector direction, ReadOnlyWorld w,
        FreerailsPrincipal principal) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = (TrackPiece)w.getTile(p.x, p.y);

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(),
                        direction);

                if (trackConfiguration != TrackConfiguration.getFlatInstance(
                            "000010000")) {
                    int owner = getOwner(principal, w);
                    newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration,
                            owner);
                } else {
                    newTrackPiece = NullTrackPiece.getInstance();
                }
            } else {
                newTrackPiece = NullTrackPiece.getInstance();
            }
        } else {
            newTrackPiece = NullTrackPiece.getInstance();
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        ChangeTrackPieceMove m = new ChangeTrackPieceMove(oldTrackPiece,
                newTrackPiece, p);

        //If we are removing a station, we also need to remove the station from the staiton list.
        if (oldTrackPiece.getTrackRule().isStation() &&
                !newTrackPiece.getTrackRule().isStation()) {
            return RemoveStationMove.getInstance(w, m, principal);
        } else {
            return m;
        }
    }

    private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(
        OneTileMoveVector direction, TrackRule trackRule, int owner) {
        TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance(
                "000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig,
                direction);

        return trackRule.getTrackPiece(trackConfiguration, owner);
    }

    public Rectangle getUpdatedTiles() {
        return updatedTiles;
    }

    public static int getOwner(FreerailsPrincipal p, ReadOnlyWorld w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            if (w.getPlayer(i).getPrincipal().equals(p)) {
                return i;
            }
        }

        throw new IllegalStateException();
    }

    /** Returns true if some track has been built.*/
    protected static boolean hasAnyTrackBeenBuilt(ReadOnlyWorld world,
        FreerailsPrincipal principal) {
        int[] unitsOfTrack = calulateNumberOfEachTrackType(world, principal);

        for (int i = 0; i < unitsOfTrack.length; i++) {
            if (0 != unitsOfTrack[i]) {
                return true;
            }
        }

        return false;
    }

    protected static boolean mustConnectToExistingTrack(ReadOnlyWorld world) {
        GameRules rules = (GameRules)world.get(ITEM.GAME_RULES);

        return rules.isMustConnect2ExistingTrack();
    }

    public static int[] calulateNumberOfEachTrackType(ReadOnlyWorld w,
        FreerailsPrincipal principal) {
        int[] unitsOfTrack = new int[w.size(SKEY.TRACK_RULES)];

        for (int i = 0; i < w.getNumberOfTransactions(principal); i++) {
            Transaction t = w.getTransaction(i, principal);

            if (t instanceof AddItemTransaction) {
                AddItemTransaction addItemTransaction = (AddItemTransaction)t;

                if (AddItemTransaction.TRACK == addItemTransaction.getCategory()) {
                    unitsOfTrack[addItemTransaction.getType()] += addItemTransaction.getQuantity();
                }
            }
        }

        return unitsOfTrack;
    }

    protected MoveStatus compositeTest(World w, FreerailsPrincipal p) {
        if (mustConnectToExistingTrack(w)) {
            if (hasAnyTrackBeenBuilt(w, this.builder)) {
                ChangeTrackPieceMove a = (ChangeTrackPieceMove)super.getMove(0);
                ChangeTrackPieceMove b = (ChangeTrackPieceMove)super.getMove(0);
                int ruleBeforeA = a.trackPieceBefore.getTrackRule()
                                                    .getRuleNumber();
                int ruleBeforeB = b.trackPieceBefore.getTrackRule()
                                                    .getRuleNumber();

                if (ruleBeforeA == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER &&
                        ruleBeforeB == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                    return MoveStatus.moveFailed(
                        "Must connect to existing track");
                }
            }
        }

        return MoveStatus.MOVE_OK;
    }
}