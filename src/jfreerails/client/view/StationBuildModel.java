/**
 * provides the models for the TrackMoveProducer build mode
 */
package jfreerails.client.view;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import jfreerails.client.renderer.TrackPieceRenderer;
import jfreerails.client.renderer.TrackPieceRendererList;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.ModelRoot;
import jfreerails.controller.StationBuilder;
import jfreerails.move.MoveStatus;
import jfreerails.util.Utils;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Money;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;

/**
 * This class provides the UI model for building a station. The mode of
 * operation is as follows:
 * <ol>
 * <li>Select a station to build by calling ActionPerformed() on the choose
 * Action.
 * <li>Set the position to build.
 * <li>call actionPerformed on the build Action
 * <li> alternatively, call actionPerformed on the cancel Action
 * </ol>
 * 
 * @author rob
 */
public class StationBuildModel {
	/*
	 * 100 010 001 = 0x111
	 */
	private static final int trackTemplate = TrackConfiguration
			.from9bitTemplate(0x111).get9bitTemplate();

	/**
	 * Vector of StationBuildAction. Actions which represent stations which can
	 * be built
	 */
	private final Vector<Action> stationChooseActions = new Vector<Action>();

	/**
	 * Whether the station's position can should change when the mouse moves.
	 */
	private boolean positionFollowsMouse = true;

	private final StationBuildAction stationBuildAction = new StationBuildAction();

	private final StationCancelAction stationCancelAction = new StationCancelAction();

	private final StationBuilder stationBuilder;

	private final ModelRoot modelRoot;

	private final HashMap<Integer, Action> id2Action = new HashMap<Integer, Action>();

	public StationBuildModel(StationBuilder sb, ViewLists vl, ModelRoot mr) {
		stationBuilder = sb;
		modelRoot = mr;

		TrackPieceRendererList trackPieceRendererList = vl
				.getTrackPieceViewList();

		ReadOnlyWorld world = modelRoot.getWorld();
		for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
			final TrackRule trackRule = (TrackRule) world.get(
					SKEY.TRACK_RULES, i);

			if (trackRule.isStation()) {
				TrackPieceRenderer renderer = trackPieceRendererList
						.getTrackPieceView(i);
				StationChooseAction action = new StationChooseAction(i);
				String trackType = trackRule.getTypeName();
				Money price = trackRule.getFixedCost();				
				String shortDescrpt = Utils.capitalizeEveryWord(trackType)
				+ " $" + price.toString();								
				action.putValue(Action.SHORT_DESCRIPTION, shortDescrpt);
				action.putValue(Action.NAME, "Build " + trackType);

				action.putValue(Action.SMALL_ICON, new ImageIcon(renderer
						.getTrackPieceIcon(trackTemplate)));
				stationChooseActions.add(action);
				id2Action.put(new Integer(i), action);
			}
		}
	}

	public Action getStationChooseAction(Integer ruleID) {
		return id2Action.get(ruleID);
	}

	public Action[] getStationChooseActions() {
		return stationChooseActions.toArray(new Action[0]);
	}

	private class StationChooseAction extends AbstractAction {
		private static final long serialVersionUID = 3257290240279458098L;

		private final int actionId;

		public StationChooseAction(int actionId) {
			this.actionId = actionId;
		}

		public void actionPerformed(ActionEvent e) {
			stationBuilder.setStationType(actionId);

			TrackRule trackRule = (TrackRule) modelRoot.getWorld().get(
					SKEY.TRACK_RULES, actionId);

			// Show the relevant station radius when the station type's menu
			// item
			// gets focus.
			stationBuildAction.putValue(StationBuildAction.STATION_RADIUS_KEY,
					new Integer(trackRule.getStationRadius()));
			stationBuildAction.setEnabled(true);
		}
	}

	private class StationCancelAction extends AbstractAction {
		private static final long serialVersionUID = 3256441421581203252L;

		public void actionPerformed(ActionEvent e) {

			stationBuildAction.setEnabled(false);
		}
	}

	/**
	 * This action builds the station.
	 */
	public class StationBuildAction extends AbstractAction {
		private static final long serialVersionUID = 3905236827739926833L;

		/**
		 * This key can be used to set the position where the station is to be
		 * built as a Point object.
		 */
		public final static String STATION_POSITION_KEY = "STATION_POSITION_KEY";

		/**
		 * This key can be used to retrieve the radius of the currently selected
		 * station as an Integer value. Don't bother writing to it!
		 */
		public final static String STATION_RADIUS_KEY = "STATION_RADIUS_KEY";

		StationBuildAction() {
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			Point value = (Point) stationBuildAction
					.getValue(StationBuildAction.STATION_POSITION_KEY);
			MoveStatus ms = stationBuilder.buildStation(new ImPoint(value.x,
					value.y));
			String message = null;

			if (ms.isOk()) {
				stationBuildAction.setEnabled(false);
			} else {
				message = ms.message;
			}

			modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, message);

		}
	}

	public boolean canBuildStationHere() {
		Point p = (Point) stationBuildAction
				.getValue(StationBuildAction.STATION_POSITION_KEY);

		return stationBuilder.tryBuildingStation(new ImPoint(p.x, p.y)).ok;
	}

	public Action getStationCancelAction() {
		return stationCancelAction;
	}

	public StationBuildAction getStationBuildAction() {
		return stationBuildAction;
	}

	public boolean isPositionFollowsMouse() {
		return positionFollowsMouse;
	}

	public void setPositionFollowsMouse(boolean positionFollowsMouse) {
		this.positionFollowsMouse = positionFollowsMouse;
	}
}