/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.controller;

import java.awt.Point;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.player.FreerailsPrincipal;
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
        ChangeTrackPieceCompositeMove move = null;
	switch (trackBuilderMode) {
	    case UPGRADE_TRACK:
		Point point = new Point(from.x + trackVector.getDx(),
			from.y + trackVector.getDy());
		return upgradeTrack(point, trackRule);
	    case BUILD_TRACK:
		move = ChangeTrackPieceCompositeMove.generateBuildTrackMove
		    (from, trackVector, trackRule, w, principal);
		break;
	    case REMOVE_TRACK:
		move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove
		    (from, trackVector, w, principal);
		break;
	    case IGNORE_TRACK:
		return MoveStatus.MOVE_OK;
	    default:
		throw new IllegalStateException("Illegal trackBuilderMode " +
			trackBuilderMode);
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
        this.trackRule = (TrackRule)w.get(KEY.TRACK_RULES, trackRuleNumber);
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
        this.trackRule = (TrackRule)w.get(KEY.TRACK_RULES, 0);
        principal = p;
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
    }

    private MoveStatus upgradeTrack(Point point, TrackRule trackRule) {
        TrackPiece before = (TrackPiece)w.getTile(point.x, point.y);
        TrackPiece after = trackRule.getTrackPiece(before.getTrackConfiguration());

        /* We don't want to 'upgrade' a station to track.  See bug 874416.*/
        if (before.getTrackRule().isStation()) {
            return MoveStatus.moveFailed("No need to upgrade track at station.");
        }

        Move move = UpgradeTrackMove.generateMove(before, after, point,
		principal);
        moveTester.processMove(transactionsGenerator.addTransactions(move));

        return moveTester.tryDoMove(move);
    }

    public int getTrackBuilderMode() {
        return trackBuilderMode;
    }
}
