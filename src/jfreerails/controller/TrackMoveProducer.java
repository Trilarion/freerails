package jfreerails.controller;

import java.awt.Point;
import java.util.Stack;
import jfreerails.move.*;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.UndoMove;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;


/** Provides methods that generate moves that build, upgrade, and remove track.
 * @author Luke
 */
final public class TrackMoveProducer {
    private TrackRule trackRule;
    private final MoveExecutor executor;
    public final static int BUILD_TRACK = 1;
    public final static int REMOVE_TRACK = 2;
    public final static int UPGRADE_TRACK = 3;

    /* Don't build any track */
    public final static int IGNORE_TRACK = 4;
    private int trackBuilderMode = BUILD_TRACK;
    private final Stack moveStack = new Stack();

    /**
    * This generates the transactions - the charge - for the track being built.
    */
    private final TrackMoveTransactionsGenerator transactionsGenerator;

    public MoveStatus buildTrack(Point from, OneTileMoveVector trackVector) {
        if (trackBuilderMode == UPGRADE_TRACK) {
            Point point = new Point(from.x + trackVector.getDx(),
                    from.y + trackVector.getDy());

            return upgradeTrack(point, trackRule);
        }

        ChangeTrackPieceCompositeMove move = null;

        FreerailsPrincipal principal = executor.getPrincipal();
        ReadOnlyWorld w = executor.getWorld();

        switch (trackBuilderMode) {
        case BUILD_TRACK: {
            move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from,
                    trackVector, trackRule, w, principal);

            break;
        }

        case REMOVE_TRACK: {
            try {
                move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(from,
                        trackVector, w, principal);
            } catch (Exception e) {
                //thrown when there is no track to remove.
                //Fix for bug [ 948670 ] Removing non-existant track
                return MoveStatus.moveFailed("No track to remove.");
            }

            break;
        }

        case IGNORE_TRACK:
            return MoveStatus.MOVE_OK;

        default:
            throw new IllegalArgumentException(String.valueOf(trackBuilderMode));
        }

        Move moveAndTransaction = transactionsGenerator.addTransactions(move);

        return sendMove(moveAndTransaction);
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
    * Sets the current track rule. E.g. there are different rules governing the
    * track-configurations that are legal for double and single track.
    *
    * @param trackRuleNumber
    *            The new trackRule value
    */
    public void setTrackRule(int trackRuleNumber) {
        ReadOnlyWorld w = executor.getWorld();
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

    public TrackMoveProducer(MoveExecutor executor, ReadOnlyWorld world) {
        this.executor = executor;

        this.trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, 0);

        FreerailsPrincipal principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
    }

    public TrackMoveProducer(MoveExecutor executor) {
        this.executor = executor;

        ReadOnlyWorld world = executor.getWorld();
        this.trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, 0);

        FreerailsPrincipal principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
    }

    private MoveStatus upgradeTrack(Point point, TrackRule trackRule) {
        ReadOnlyWorld w = executor.getWorld();
        TrackPiece before = (TrackPiece)w.getTile(point.x, point.y);
        FreerailsPrincipal principal = executor.getPrincipal();
        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, w);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(),
                trackRule, owner);

        /* We don't want to 'upgrade' a station to track. See bug 874416. */
        if (before.getTrackRule().isStation()) {
            return MoveStatus.moveFailed("No need to upgrade track at station.");
        }

        Move move = UpgradeTrackMove.generateMove(before, after, point);
        Move move2 = transactionsGenerator.addTransactions(move);

        return sendMove(move2);
    }

    public MoveStatus undoLastTrackMove() {
        if (moveStack.size() > 0) {
            Move m = (Move)moveStack.pop();
            UndoMove undoMove = new UndoMove(m);
            MoveStatus ms = executor.doMove(undoMove);

            if (!ms.ok) {
                return MoveStatus.moveFailed("Can not undo building track!");
            } else {
                return ms;
            }
        } else {
            return MoveStatus.moveFailed("No track to undo building!");
        }
    }

    public int getTrackBuilderMode() {
        return trackBuilderMode;
    }

    private MoveStatus sendMove(Move m) {
        MoveStatus ms = executor.doMove(m);

        if (ms.isOk()) {
            moveStack.add(m);
        }

        return ms;
    }
}