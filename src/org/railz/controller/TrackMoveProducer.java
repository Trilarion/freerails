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

package org.railz.controller;

import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.move.ChangeTrackPieceCompositeMove;
import org.railz.move.Move;
import org.railz.move.MoveStatus;
import org.railz.move.UpgradeTrackMove;
import org.railz.world.common.CompassPoints;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.TrackTile;

final public class TrackMoveProducer {
	private int trackRule;
	private final ReadOnlyWorld w;

	private static final String CLASS_NAME = TrackMoveProducer.class.getName();
	private static final Logger LOGGER = LogManager.getLogger(CLASS_NAME);

	/**
	 * The principal on behalf of which this TrackMoveProducer is producing
	 * moves
	 */
	private final FreerailsPrincipal principal;
	private final UntriedMoveReceiver moveTester;
	public final static int BUILD_TRACK = 1;
	public final static int REMOVE_TRACK = 2;
	public final static int UPGRADE_TRACK = 3;

	/* Don't build any track */
	public final static int IGNORE_TRACK = 4;
	private int trackBuilderMode = IGNORE_TRACK;

	/**
	 * @param p
	 *            the principal which this TrackMoveProducer generates moves for
	 */
	public TrackMoveProducer(ReadOnlyWorld world,
			UntriedMoveReceiver moveReceiver, FreerailsPrincipal p) {
		if (null == world || null == moveReceiver) {
			throw new NullPointerException();
		}

		this.moveTester = moveReceiver;
		this.w = world;
		this.trackRule = 0;
		principal = p;
		transactionsGenerator = new TrackMoveTransactionsGenerator(world,
				principal);
	}

	/**
	 * This generates the transactions - the charge - for the track being built.
	 */
	private final TrackMoveTransactionsGenerator transactionsGenerator;

	/**
	 * @param trackVector
	 *            a CompassPoints representing a single direction in which to
	 *            add track
	 */
	public MoveStatus buildTrack(Point from, byte trackVector) {
		final String methodName = "buildTrack";

		ChangeTrackPieceCompositeMove move = null;

		switch (trackBuilderMode) {
		case UPGRADE_TRACK:
			// TODO Upgrade needs to upgrade on both relevent squares (not one)
			// See remove for generating composite move
			Point point = new Point(from.x
					+ CompassPoints.getUnitDeltaX(trackVector), from.y
					+ CompassPoints.getUnitDeltaY(trackVector));
			return upgradeTrack(point, trackRule);
		case BUILD_TRACK:
			try {
				move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(
						from, trackVector, trackRule, w, principal);
			} catch (IllegalArgumentException e) {
				return MoveStatus.moveFailed("Track already exists");
			}
			break;
		case REMOVE_TRACK:
			try {
				move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(
						from, trackVector, w, principal);
			} catch (IllegalArgumentException ex) {
				LOGGER.logp(Level.INFO, CLASS_NAME, methodName,
						"Invalid data building move.");
				return MoveStatus.moveFailed("Track does not exist");
			}

			break;
		case IGNORE_TRACK:
			return MoveStatus.MOVE_OK;
		default:
			throw new IllegalStateException("Illegal trackBuilderMode "
					+ trackBuilderMode);
		}
		MoveStatus ms = null;
		if (move != null) {
			Move moveAndTransaction = transactionsGenerator
					.addTransactions(move);

			ms = moveTester.tryDoMove(moveAndTransaction);
			moveTester.processMove(moveAndTransaction);
		} else {
			ms = MoveStatus.moveFailed("Investigate why move is null.");
		}

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
	 * Sets the current track rule. E.g. there are different rules governing the
	 * track-configurations that are legal for double and single track.
	 * 
	 * @param trackRuleNumber
	 *            The new trackRule value
	 */
	public void setTrackRule(int trackRuleNumber) {
		trackRule = trackRuleNumber;
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

	public int getTrackBuilderMode() {
		return trackBuilderMode;
	}

	public boolean isBuilding() {
		if (IGNORE_TRACK == trackBuilderMode) {
			return false;
		} else {
			return true;
		}
	}

	private MoveStatus upgradeTrack(Point point, int trackRule) {
		TrackTile before = w.getTile(point.x, point.y).getTrackTile();
		if (before == null)
			return MoveStatus.moveFailed("Can't upgrade non-existent track");

		TrackTile after = TrackTile.createTrackTile(w,
				before.getTrackConfiguration(), trackRule);
		if (before.equals(after))
			return MoveStatus.moveFailed("Can't upgrade track with identical"
					+ " type");

		Move move = UpgradeTrackMove.generateMove(before, after, point,
				principal);
		MoveStatus ms = moveTester.tryDoMove(move);
		moveTester.processMove(transactionsGenerator.addTransactions(move));

		return ms;
	}

}
