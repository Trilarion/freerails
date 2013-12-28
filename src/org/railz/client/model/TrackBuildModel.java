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

package org.railz.client.model;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.railz.client.common.ActionAdapter;
import org.railz.client.renderer.TrackPieceRenderer;
import org.railz.client.renderer.TrackPieceRendererList;
import org.railz.client.renderer.ViewLists;
import org.railz.controller.TrackMoveProducer;
import org.railz.world.common.CompassPoints;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.TrackRule;

/**
 * provides the models for the TrackMoveProducer build mode
 */
public class TrackBuildModel {
	private static final byte trackTemplate = CompassPoints.NORTHWEST
			| CompassPoints.SOUTHEAST;

	private final ActionAdapter buildModeAdapter;

	private final ActionAdapter trackRuleAdapter;

	private final TrackMoveProducer trackMoveProducer;

	private final ViewLists viewLists;

	private final ReadOnlyWorld world;

	public ActionAdapter getBuildModeActionAdapter() {
		return buildModeAdapter;
	}

	public ActionAdapter getTrackRuleAdapter() {
		return trackRuleAdapter;
	}

	// Make Generic Action that can be subclassed to avoid code duplication

	private class BuildModeAction extends AbstractAction {
		final String BUILD_MODE_PREFIX = "buildMode_";

		private final int actionId;

		private BuildModeAction(int actionId, String name) {
			putValue(NAME, name);
			putValue(ACTION_COMMAND_KEY, name);
			this.actionId = actionId;

			Image image = getTileImage(actionId);
			putValue(SMALL_ICON, new ImageIcon(image));

			String tooltip = returnTooltip(actionId);
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		private Image getTileImage(int ruleNumber) {
			String imageName = "";

			switch (ruleNumber) {
			case (1):
				imageName = BUILD_MODE_PREFIX + "build";
				break;
			case (2):
				imageName = BUILD_MODE_PREFIX + "remove";
				break;
			case (3):
				imageName = BUILD_MODE_PREFIX + "upgrade";
				break;
			case (4):
				imageName = BUILD_MODE_PREFIX + "view";
				break;
			default:
				imageName = BUILD_MODE_PREFIX + "build";
			}

			ImageIcon icon = viewLists.getImageIcon(imageName);
			Image tileImage = icon.getImage();

			return tileImage;
		}

		private String returnTooltip(int actionId) {
			// TODO tooltip
			TrackRule trackRule = (TrackRule) world.get(KEY.TRACK_RULES,
					actionId);
			final String TRACK_SUFFIX = " track";
			String tooltip = "tooltip";
			if (actionId == 1) {
				tooltip = "build" + TRACK_SUFFIX;
			} else if (actionId == 2) {
				tooltip = "remove" + TRACK_SUFFIX;
			} else if (actionId == 3) {
				tooltip = "upgrade" + TRACK_SUFFIX;
			} else if (actionId == 4) {
				tooltip = "view mode";
			} else {
				tooltip = "build";
			}

			return tooltip;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!(e.getSource() instanceof ActionAdapter))
				return;

			trackMoveProducer.setTrackBuilderMode(actionId);
		}
	}

	// TODO action is created here... understand
	private class TrackRuleAction extends AbstractAction {
		private final int actionId;

		private TrackRuleAction(int actionId, String name) {
			putValue(NAME, name);
			putValue(ACTION_COMMAND_KEY, name);
			this.actionId = actionId;

			// TODO polymorphism
			Image image = getTileImage(actionId);
			putValue(SMALL_ICON, new ImageIcon(image));

			String tooltip = returnTooltip();
			putValue(SHORT_DESCRIPTION, tooltip);
		}

		private String returnTooltip() {
			TrackRule trackRule = (TrackRule) world.get(KEY.TRACK_RULES,
					actionId);
			return trackRule.toString() + " @ $" + trackRule.getPrice();
		}

		private Image getTileImage(int ruleNumber) {
			// Add image to button... assumption...
			TrackPieceRendererList trackPieceRendererList = viewLists
					.getTrackPieceViewList();
			TrackPieceRenderer renderer = trackPieceRendererList
					.getTrackPieceView(ruleNumber);

			/* create a scaled image */
			BufferedImage unscaledImage = renderer
					.getTrackPieceIcon(trackTemplate);
			Image scaledImage = unscaledImage.getScaledInstance(
					unscaledImage.getWidth() * 3 / 4,
					unscaledImage.getHeight() * 3 / 4, Image.SCALE_SMOOTH);

			return scaledImage;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!(e.getSource() instanceof ActionAdapter))
				return;

			trackMoveProducer.setTrackRule(actionId);
		}
	}

	public TrackBuildModel(TrackMoveProducer tmp, ReadOnlyWorld world,
			ViewLists vl) {
		this.world = world;
		viewLists = vl;
		trackMoveProducer = tmp;

		/* set up build modes */
		BuildModeAction[] actions = new BuildModeAction[] {
				new BuildModeAction(TrackMoveProducer.BUILD_TRACK,
						"Build Track"),
				new BuildModeAction(TrackMoveProducer.REMOVE_TRACK,
						"Remove Track"),
				new BuildModeAction(TrackMoveProducer.UPGRADE_TRACK,
						"Upgrade Track"),
				new BuildModeAction(TrackMoveProducer.IGNORE_TRACK, "View Mode") };
		buildModeAdapter = new ActionAdapter(actions);

		/* set up track actions */
		Vector actionsVector = new Vector();
		for (int i = 0; i < world.size(KEY.TRACK_RULES); i++) {
			TrackRule trackRule = (TrackRule) world.get(KEY.TRACK_RULES, i);
			actionsVector.add(new TrackRuleAction(i, trackRule.toString()));
		}
		trackRuleAdapter = new ActionAdapter(
				(Action[]) actionsVector.toArray(new Action[0]));
	}
}
