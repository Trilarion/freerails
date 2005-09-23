/*
 * Created on 15-Aug-2005
 *
 */
package jfreerails.client.renderer;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.controller.BuildTrackStrategy;
import jfreerails.controller.MoveExecutor;
import jfreerails.controller.SimpleMoveExecutor;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.ModelRoot.Property;
import jfreerails.controller.TrackMoveProducer.BuildMode;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;
import junit.framework.TestCase;

/** Unit test for BuildTrackController. */

public class BuildTrackControllerTest extends TestCase {

	World w;

	ModelRootImpl modelRoot;

	BuildTrackController buildTrackController;

	TrackMoveProducer trackBuilder;

	int singleTrackRuleID = -1;

	int doubleTrackRuleID = -1;

	protected void setUp() throws Exception {
		w = MapFixtureFactory2.getCopy();
		modelRoot = new ModelRootImpl();
		FreerailsPrincipal p = w.getPlayer(0).getPrincipal();
		modelRoot.setup(w, p);
		buildTrackController = new BuildTrackController(w, modelRoot);
		MoveExecutor executor = new SimpleMoveExecutor(w, 0);
		trackBuilder = new TrackMoveProducer(executor, w, modelRoot);

		for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {

			final Integer ruleID = new Integer(i);
			TrackRule rule = (TrackRule) w.get(SKEY.TRACK_RULES, i);

			if (rule.getTypeName().equals("standard track")) {
				singleTrackRuleID = ruleID;
			}
			if (rule.getTypeName().equals("double track")) {
				doubleTrackRuleID = ruleID;
			}

		}
		assertFalse(singleTrackRuleID == -1);
		assertFalse(doubleTrackRuleID == -1);

		// unit tests should be silent!
		modelRoot.setProperty(Property.PLAY_SOUNDS, false);
	}

	public void testBuildTrack() {
		ImPoint from = new ImPoint(10, 10);
		modelRoot.setProperty(Property.CURSOR_POSITION, from);
		ImPoint to = new ImPoint(20, 10);
		buildTrackController.setProposedTrack(to, trackBuilder);

		buildTrackController.updateUntilComplete();

		assertTrue(buildTrackController.isBuildTrackSuccessful());

		// See if any track has actually been built.
		FreerailsTile tile = (FreerailsTile) w.getTile(10, 10);
		assertFalse(tile.hasTrack());
		buildTrackController.updateWorld(trackBuilder);
		tile = (FreerailsTile) w.getTile(10, 10);
		assertTrue(tile.hasTrack());
		tile = (FreerailsTile) w.getTile(20, 10);
		assertTrue(tile.hasTrack());

	}

	public void testUpgradeTrack() {
		// Build the track.
		testBuildTrack();

		// Change the strategy.
		BuildTrackStrategy bts = BuildTrackStrategy.getSingleRuleInstance(
				doubleTrackRuleID, modelRoot.getWorld());

		trackBuilder.setBuildTrackStrategy(bts);
		trackBuilder.setTrackBuilderMode(BuildMode.UPGRADE_TRACK);

		// Upgrade part of the track.
		modelRoot.setProperty(Property.CURSOR_POSITION, new ImPoint(15, 10));
		buildTrackController
				.setProposedTrack(new ImPoint(20, 10), trackBuilder);
		buildTrackController.updateUntilComplete();

		assertTrue(buildTrackController.isBuildTrackSuccessful());

		buildTrackController.updateWorld(trackBuilder);

		FreerailsTile tile = (FreerailsTile) w.getTile(10, 10);

		assertEquals(singleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

		tile = (FreerailsTile) w.getTile(15, 10);

		assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

		tile = (FreerailsTile) w.getTile(17, 10);

		assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

		tile = (FreerailsTile) w.getTile(20, 10);

		assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

	}

	public void testRemoveTrack() {
		// Build the track.
		testBuildTrack();

		// Then remove some of it.
		trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
		ImPoint from = new ImPoint(15, 10);
		modelRoot.setProperty(Property.CURSOR_POSITION, from);

		ImPoint to = new ImPoint(20, 10);
		buildTrackController.setProposedTrack(to, trackBuilder);

		buildTrackController.updateUntilComplete();

		assertTrue(buildTrackController.isBuildTrackSuccessful());

		buildTrackController.updateWorld(trackBuilder);

	}

}
