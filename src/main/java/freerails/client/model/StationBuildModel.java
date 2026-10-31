/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.client.MoneyFormatter;
import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.client.ModelRoot;
import freerails.model.track.TrackType;
import freerails.controller.StationBuilder;
import freerails.move.Status;
import freerails.util.Vector2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.economy.Money;
import freerails.model.track.TrackConfiguration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
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
    private final Map<Integer, Action> idToAction = new HashMap<>();
    /**
     * Whether the station's position can should change when the mouse moves.
     */
    private boolean positionFollowsMouse = true;

    /**
     * Creates a station build model instance.
     *
     * @param stationBuilder the station builder parameter
     * @param rendererRoot the renderer root parameter
     * @param modelRoot the client model that provides current game state
     */
    public StationBuildModel(StationBuilder stationBuilder, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.stationBuilder = stationBuilder;
        this.modelRoot = modelRoot;

        UnmodifiableWorld world = this.modelRoot.getWorld();

        for (TrackType trackType: world.getTrackTypes()) {

            if (trackType.isStation()) {
                TrackPieceRenderer renderer = rendererRoot.getTrackPieceView(trackType.getId());
                Action action = new StationChooseAction(trackType.getId());
                String name = trackType.getName();
                Money price = trackType.getPurchasingPrice();
                String shortDescrpt = name + " " + MoneyFormatter.format(price);
                action.putValue(Action.SHORT_DESCRIPTION, shortDescrpt);
                action.putValue(Action.NAME, "Build " + name);

                action.putValue(Action.SMALL_ICON, new ImageIcon(renderer.getTrackPieceIcon(trackTemplate)));
                stationChooseActions.add(action);
                idToAction.put(trackType.getId(), action);
            }
        }
    }

    /**
     * Returns the station choose action.
     *
     * @param ruleID the rule id parameter
     * @return the station choose action produced by this method
     */
    public Action getStationChooseAction(Integer ruleID) {
        return idToAction.get(ruleID);
    }

    /**
     * Returns the station choose actions.
     *
     * @return the station choose actions produced by this method
     */
    public Action[] getStationChooseActions() {
        return stationChooseActions.toArray(new Action[0]);
    }

    /**
     * Returns whether this object can build station here.
     *
     * @return {@code true} if build station here; otherwise, {@code false}
     */
    public boolean canBuildStationHere() {
        java.awt.Point p = (java.awt.Point) stationBuildAction.getValue(StationBuildAction.STATION_POSITION_KEY);

        return stationBuilder.tryBuildingStation(new Vector2D(p.x, p.y)).isSuccess();
    }

    /**
     * Returns the station cancel action.
     *
     * @return the station cancel action produced by this method
     */
    public ActionListener getStationCancelAction() {
        return stationCancelAction;
    }

    /**
     * Returns the station build action.
     *
     * @return the station build action produced by this method
     */
    public StationBuildAction getStationBuildAction() {
        return stationBuildAction;
    }

    /**
     * Returns whether position follows mouse.
     *
     * @return {@code true} if position follows mouse; otherwise, {@code false}
     */
    public boolean isPositionFollowsMouse() {
        return positionFollowsMouse;
    }

    /**
     * Sets the position follows mouse.
     *
     * @param positionFollowsMouse the position follows mouse parameter
     */
    public void setPositionFollowsMouse(boolean positionFollowsMouse) {
        this.positionFollowsMouse = positionFollowsMouse;
    }

    /**
     * Provides the station choose action functionality.
     */
    private class StationChooseAction extends AbstractAction {
        @Serial
        private static final long serialVersionUID = 3257290240279458098L;

        private final int actionId;

        /**
         * Creates a station choose action instance.
         * @param actionId the action id
         */
        private StationChooseAction(int actionId) {
            this.actionId = actionId;
        }

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            stationBuilder.setStationType(actionId);

            TrackType trackRule = modelRoot.getWorld().getTrackType(actionId);

            // Show the relevant station radius when the station type's menu item gets focus.
            stationBuildAction.putValue(StationBuildAction.STATION_RADIUS_KEY, trackRule.getStationRadius());
            stationBuildAction.setEnabled(true);
        }
    }

    /**
     * Provides the station cancel action functionality.
     */
    private class StationCancelAction extends AbstractAction {
        @Serial
        private static final long serialVersionUID = 3256441421581203252L;

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
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
        @Serial
        private static final long serialVersionUID = 3905236827739926833L;

        /**
         * Creates a station build action instance.
         */
        private StationBuildAction() {
            setEnabled(false);
        }

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            java.awt.Point value = (java.awt.Point) stationBuildAction.getValue(StationBuildAction.STATION_POSITION_KEY);
            Status status = stationBuilder.buildStation(new Vector2D(value.x, value.y));
            String message = null;

            if (status.isSuccess()) {
                stationBuildAction.setEnabled(false);
            } else {
                message = status.getMessage();
            }

            modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, message);
        }
    }
}