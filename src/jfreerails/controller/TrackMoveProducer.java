package jfreerails.controller;

import java.awt.Point;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


final public class TrackMoveProducer {
    private TrackRule trackRule;
    private ReadOnlyWorld w;

    /**
     * The principal on behalf of which this TrackMoveProducer is producing
     * moves
     */
    private FreerailsPrincipal principal;
    private UntriedMoveReceiver moveTester;
    public final static int BUILD_TRACK = 1;
    public final static int REMOVE_TRACK = 2;
    public final static int UPGRADE_TRACK = 3;

    /* Don't build any track */
    public final static int IGNORE_TRACK = 4;
    private int trackBuilderMode = BUILD_TRACK;

    /**
     * This generates the transactions - the charge - for the track being
     * built.
     */
    private TrackMoveTransactionsGenerator transactionsGenerator;

    public MoveStatus buildTrack(Point from, OneTileMoveVector trackVector) {
        if (trackBuilderMode == UPGRADE_TRACK) {
            Point point = new Point(from.x + trackVector.getDx(),
                    from.y + trackVector.getDy());

            return upgradeTrack(point, trackRule);
        }

        ChangeTrackPieceCompositeMove move = null;

        if (trackBuilderMode == BUILD_TRACK) {
            move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from,
                    trackVector, trackRule, w, this.principal);
        } else if (trackBuilderMode == REMOVE_TRACK) {
            move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(from,
                    trackVector, w, principal);
        } else {
            throw new IllegalArgumentException(String.valueOf(trackBuilderMode));
        }

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);
        MoveStatus ms = moveTester.tryDoMove(moveAndTransaction);
        moveTester.processMove(moveAndTransaction);

        return ms;
    }

    public MoveStatus upgradeTrack(Point point) {
        if (trackBuilderMode == UPGRADE_TRACK) {
            return upgradeTrack(point, trackRule);
        } else {
            throw new IllegalStateException(
                "Track builder not set to upgrade track!");
        }
    }

    /**
     *  Sets the current track rule. E.g. there are different rules governing
     *  the track-configurations that are legal for double and single track.
     *
     *@param  trackRuleNumber  The new trackRule value
     */
    public void setTrackRule(int trackRuleNumber) {
        this.trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, trackRuleNumber);
    }

    public void setTrackBuilderMode(int i) {
        switch (i) {
        case BUILD_TRACK:
        case REMOVE_TRACK:
        case UPGRADE_TRACK:
        case IGNORE_TRACK:
            trackBuilderMode = i;

            break;

        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param p the principal which this TrackMoveProducer generates moves for
     */
    public TrackMoveProducer(ReadOnlyWorld world,
        UntriedMoveReceiver moveReceiver, FreerailsPrincipal p) {
        if (null == world || null == moveReceiver) {
            throw new NullPointerException();
        }

        this.moveTester = moveReceiver;
        this.w = world;
        this.trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, 0);
        principal = p;
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
    }

    private MoveStatus upgradeTrack(Point point, TrackRule trackRule) {
        TrackPiece before = (TrackPiece)w.getTile(point.x, point.y);
        int owner = ChangeTrackPieceCompositeMove.getOwner(this.principal, w);
        TrackPiece after = trackRule.getTrackPiece(before.getTrackConfiguration(),
                owner);

        /* We don't want to 'upgrade' a station to track.  See bug 874416.*/
        if (before.getTrackRule().isStation()) {
            return MoveStatus.moveFailed("No need to upgrade track at station.");
        }

        Move move = UpgradeTrackMove.generateMove(before, after, point);
        moveTester.processMove(transactionsGenerator.addTransactions(move));

        return moveTester.tryDoMove(move);
    }

    public int getTrackBuilderMode() {
        return trackBuilderMode;
    }
}