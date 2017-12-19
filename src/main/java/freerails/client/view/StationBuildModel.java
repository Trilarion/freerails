/*
  provides the models for the TrackMoveProducer build mode
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.client.renderer.TrackPieceRenderer;
import freerails.controller.ModelRoot;
import freerails.controller.StationBuilder;
import freerails.move.MoveStatus;
import freerails.util.Utils;
import freerails.world.common.ImPoint;
import freerails.world.common.Money;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private final List<Action> stationChooseActions = new ArrayList<>();
    private final StationBuildAction stationBuildAction = new StationBuildAction();
    private final StationCancelAction stationCancelAction = new StationCancelAction();
    private final StationBuilder stationBuilder;
    private final ModelRoot modelRoot;
    private final HashMap<Integer, Action> id2Action = new HashMap<>();
    /**
     * Whether the station's position can should change when the mouse moves.
     */
    private boolean positionFollowsMouse = true;

    /**
     *
     * @param sb
     * @param rr
     * @param mr
     */
    public StationBuildModel(StationBuilder sb, RenderersRoot rr, ModelRoot mr) {
        stationBuilder = sb;
        modelRoot = mr;

        ReadOnlyWorld world = modelRoot.getWorld();
        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            final TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES,
                    i);

            if (trackRule.isStation()) {
                TrackPieceRenderer renderer = rr.getTrackPieceView(i);
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
                id2Action.put(i, action);
            }
        }
    }

    /**
     *
     * @param ruleID
     * @return
     */
    public Action getStationChooseAction(Integer ruleID) {
        return id2Action.get(ruleID);
    }

    /**
     *
     * @return
     */
    public Action[] getStationChooseActions() {
        return stationChooseActions.toArray(new Action[stationChooseActions
                .size()]);
    }

    /**
     *
     * @return
     */
    public boolean canBuildStationHere() {
        Point p = (Point) stationBuildAction
                .getValue(StationBuildAction.STATION_POSITION_KEY);

        return stationBuilder.tryBuildingStation(new ImPoint(p.x, p.y)).ok;
    }

    /**
     *
     * @return
     */
    public Action getStationCancelAction() {
        return stationCancelAction;
    }

    /**
     *
     * @return
     */
    public StationBuildAction getStationBuildAction() {
        return stationBuildAction;
    }

    /**
     *
     * @return
     */
    public boolean isPositionFollowsMouse() {
        return positionFollowsMouse;
    }

    /**
     *
     * @param positionFollowsMouse
     */
    public void setPositionFollowsMouse(boolean positionFollowsMouse) {
        this.positionFollowsMouse = positionFollowsMouse;
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
                    trackRule.getStationRadius());
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
         * built as a Point object.
         */
        public final static String STATION_POSITION_KEY = "STATION_POSITION_KEY";
        /**
         * This key can be used to retrieve the radius of the currently selected
         * station as an Integer value. Don't bother writing to it!
         */
        public final static String STATION_RADIUS_KEY = "STATION_RADIUS_KEY";
        private static final long serialVersionUID = 3905236827739926833L;

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
}