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
  provides the models for the TrackMoveProducer build mode
 */
package freerails.client.model;

import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.ModelRoot;
import freerails.move.StationBuilder;
import freerails.move.MoveStatus;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.finances.Money;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackRule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the UI model for building a station. The mode of
 * operation is as follows:
 * <ol>
 * <li>Select a station to build by calling ActionPerformed() on the choose
 * Action.
 * <li>Set the position to build.
 * <li>call actionPerformed on the build Action
 * <li> alternatively, call actionPerformed on the cancel Action
 * </ol>
 */
public class StationBuildModel {

    /*
     * 100 010 001 = 0x111
     */
    private static final int trackTemplate = TrackConfiguration.from9bitTemplate(0x111).get9bitTemplate();

    /**
     * List of StationBuildAction. Actions which represent stations which can
     * be built
     */
    private final List<Action> stationChooseActions = new ArrayList<>();
    private final StationBuildAction stationBuildAction = new StationBuildAction();
    private final ActionListener stationCancelAction = new StationCancelAction();
    private final StationBuilder stationBuilder;
    private final ModelRoot modelRoot;
    private final Map<Integer, Action> id2Action = new HashMap<>();
    /**
     * Whether the station's position can should change when the mouse moves.
     */
    private boolean positionFollowsMouse = true;

    /**
     * @param stationBuilder
     * @param rendererRoot
     * @param modelRoot
     */
    public StationBuildModel(StationBuilder stationBuilder, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.stationBuilder = stationBuilder;
        this.modelRoot = modelRoot;

        ReadOnlyWorld world = this.modelRoot.getWorld();
        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            final TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, i);

            if (trackRule.isStation()) {
                TrackPieceRenderer renderer = rendererRoot.getTrackPieceView(i);
                Action action = new StationChooseAction(i);
                String trackType = trackRule.getTypeName();
                Money price = trackRule.getFixedCost();
                String shortDescrpt = trackType + " $" + price.toString();
                action.putValue(Action.SHORT_DESCRIPTION, shortDescrpt);
                action.putValue(Action.NAME, "Build " + trackType);

                action.putValue(Action.SMALL_ICON, new ImageIcon(renderer.getTrackPieceIcon(trackTemplate)));
                stationChooseActions.add(action);
                id2Action.put(i, action);
            }
        }
    }

    /**
     * @param ruleID
     * @return
     */
    public Action getStationChooseAction(Integer ruleID) {
        return id2Action.get(ruleID);
    }

    /**
     * @return
     */
    public Action[] getStationChooseActions() {
        return stationChooseActions.toArray(new Action[stationChooseActions.size()]);
    }

    /**
     * @return
     */
    public boolean canBuildStationHere() {
        java.awt.Point p = (java.awt.Point) stationBuildAction.getValue(StationBuildAction.STATION_POSITION_KEY);

        return stationBuilder.tryBuildingStation(new Vector2D(p.x, p.y)).succeeds();
    }

    /**
     * @return
     */
    public ActionListener getStationCancelAction() {
        return stationCancelAction;
    }

    /**
     * @return
     */
    public StationBuildAction getStationBuildAction() {
        return stationBuildAction;
    }

    /**
     * @return
     */
    public boolean isPositionFollowsMouse() {
        return positionFollowsMouse;
    }

    /**
     * @param positionFollowsMouse
     */
    public void setPositionFollowsMouse(boolean positionFollowsMouse) {
        this.positionFollowsMouse = positionFollowsMouse;
    }

    private class StationChooseAction extends AbstractAction {
        private static final long serialVersionUID = 3257290240279458098L;

        private final int actionId;

        private StationChooseAction(int actionId) {
            this.actionId = actionId;
        }

        public void actionPerformed(ActionEvent e) {
            stationBuilder.setStationType(actionId);

            TrackRule trackRule = (TrackRule) modelRoot.getWorld().get(SharedKey.TrackRules, actionId);

            // Show the relevant station radius when the station type's menu
            // item
            // gets focus.
            stationBuildAction.putValue(StationBuildAction.STATION_RADIUS_KEY, trackRule.getStationRadius());
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
        /**
         * This key can be used to set the position where the station is to be
         * built as a Point2D object.
         */
        public static final String STATION_POSITION_KEY = "STATION_POSITION_KEY";
        /**
         * This key can be used to retrieve the radius of the currently selected
         * station as an Integer value. Don't bother writing to it!
         */
        public static final String STATION_RADIUS_KEY = "STATION_RADIUS_KEY";
        private static final long serialVersionUID = 3905236827739926833L;

        private StationBuildAction() {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            java.awt.Point value = (java.awt.Point) stationBuildAction.getValue(StationBuildAction.STATION_POSITION_KEY);
            MoveStatus moveStatus = stationBuilder.buildStation(new Vector2D(value.x, value.y));
            String message = null;

            if (moveStatus.succeeds()) {
                stationBuildAction.setEnabled(false);
            } else {
                message = moveStatus.getMessage();
            }

            modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, message);
        }
    }
}