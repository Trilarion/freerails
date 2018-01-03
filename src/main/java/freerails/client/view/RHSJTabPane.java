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

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.common.ModelRootImpl;
import freerails.client.common.ModelRootListener;
import freerails.client.common.StationHelper;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.util.ImPoint;
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.FreerailsTile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * The tabbed panel that sits in the lower right hand corner of the screen.
 */
public class RHSJTabPane extends JTabbedPane implements ModelRootListener {

    private static final long serialVersionUID = 3906926798502965297L;

    private static final Logger LOGGER = Logger
            .getLogger(RHSJTabPane.class.getName());

    private final TerrainInfoJPanel terrainInfoPanel;

    private final StationInfoJPanel stationInfoPanel;

    private final TrainListJPanel trainListPanel;

    private final BuildTrackJPanel buildTrackPanel;
    private final int terrainInfoIndex;
    private final int trainListIndex;
    private final int stationInfoIndex;
    private ReadOnlyWorld world;
    private ModelRoot modelRoot;

    /**
     *
     */
    public RHSJTabPane() {
        /*
         * Don't accept keyboard focus since we want to leave it with the main
         * map view.
         */
        setFocusable(false);

        ImageIcon trainListIcon;
        ImageIcon buildTrackIcon;
        ImageIcon stationInfoIcon;

        /* set up trainsJTabbedPane */
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        terrainInfoPanel = new TerrainInfoJPanel();

        trainListPanel = new TrainListJPanel(true);

        buildTrackPanel = new BuildTrackJPanel();
        trainListPanel.removeButtons();

        URL terrainInfoIconUrl = getClass().getResource(
                ClientConfig.ICON_TERRAIN_INFO);
        ImageIcon terrainInfoIcon = new ImageIcon(terrainInfoIconUrl);

        URL buildTrackIconUrl = getClass().getResource(
                ClientConfig.ICON_NEW_TRACK);
        buildTrackIcon = new ImageIcon(buildTrackIconUrl);

        URL trainListIconUrl = getClass().getResource(
                ClientConfig.ICON_TRAIN_LIST);
        trainListIcon = new ImageIcon(trainListIconUrl);

        URL stationListIconUrl = getClass().getResource(
                ClientConfig.ICON_STATION_LIST);
        stationInfoIcon = new ImageIcon(stationListIconUrl);

        // Note titles set to null so only the icon appears at the top of the
        // top.
        JScrollPane terrainInfoJScrollPane = new JScrollPane(terrainInfoPanel);
        terrainInfoJScrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(null, terrainInfoIcon, terrainInfoJScrollPane, "Terrain Info");
        this.terrainInfoIndex = this.getTabCount() - 1;

        stationInfoPanel = new StationInfoJPanel();
        stationInfoPanel.removeCloseButton();
        // Don't show the station info tab until it has been rewritten to take
        // up less space.
        JScrollPane stationInfoJScrollPane = new JScrollPane(stationInfoPanel);
        stationInfoJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(null, stationInfoIcon, stationInfoJScrollPane, "Station Info");
        this.stationInfoIndex = this.getTabCount() - 1;

        trainListPanel.setTrainViewHeight(20);
        addTab(null, buildTrackIcon, buildTrackPanel, "Build Track");
        addTab(null, trainListIcon, trainListPanel, "Train List");
        this.trainListIndex = this.getTabCount() - 1;

        /* These values were picked by trial and error! */
        this.setMinimumSize(new Dimension(250, 200));

    }

    /**
     * @param actionRoot
     * @param vl
     * @param modelRoot
     */
    public void setup(final ActionRoot actionRoot, RendererRoot vl,
                      final ModelRootImpl modelRoot) {

        this.modelRoot = modelRoot;
        world = modelRoot.getWorld();

        terrainInfoPanel.setup(world, vl);
        stationInfoPanel.setup(modelRoot, vl, null);

        ActionListener showTrain = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int id = trainListPanel.getSelectedTrainID();
                actionRoot.getDialogueBoxController().showTrainOrders(id);
            }
        };

        trainListPanel.setShowTrainDetailsActionListener(showTrain);
        trainListPanel.setup(modelRoot, vl, null);
        modelRoot.addPropertyChangeListener(this);

        buildTrackPanel.setup(modelRoot, actionRoot);
    }

    /**
     * Updates the Terrain Info Panel if the specified PropertyChangeEvent was
     * triggered by the cursor moving.
     *
     * @param prop
     * @param before
     * @param after
     */
    public void propertyChange(ModelRoot.Property prop, Object before,
                               Object after) {
        if (prop.equals(ModelRoot.Property.CURSOR_POSITION)) {

            ImPoint p = (ImPoint) after;

            int x = p.x;
            int y = p.y;

            // Select priority element at location
            LOGGER.debug("Let's try to show the station.");

//            FreerailsSerializable freerailsSerializable = world.get(SKEY.TRACK_RULES, 0);
//            if (freerailsSerializable != null) {
//                LOGGER.info("Track piece at location.");
//                
//                LOGGER.info("Type is: " + freerailsSerializable.getClass().getName());
//                
//                if (freerailsSerializable instanceof TrackRule) {
//                    TrackRule trackRule = (TrackRule) freerailsSerializable;
//                    boolean station = trackRule.isStation();
//                    LOGGER.info("isStation: " + station);   
//                }
//
//            }
//            else {
//                LOGGER.info("No piece at location.");
//            }


            // select station at point and show stat info tab
            // if not, then do terrain info and show that
            int stationNumberAtLocation = StationHelper.getStationNumberAtLocation(world, modelRoot, x, y);
            if (stationNumberAtLocation > -1) {
                LOGGER.info("stationNumber: " + stationNumberAtLocation);
                stationInfoPanel.setStation(stationNumberAtLocation);
                this.setSelectedIndex(stationInfoIndex);
            } else {
                //terrainInfoPanel.showTerrainInfo(x, y);
                LOGGER.info("Default behaviour show terrain.");
                terrainInfoPanel.setTerrainType(((FreerailsTile) world.getTile(p.x,
                        p.y)).getTerrainTypeID());
                this.setSelectedIndex(terrainInfoIndex);
            }

        }
    }

    /**
     * @param enabled
     */
    public void setTrainTabEnabled(boolean enabled) {
        this.setEnabledAt(this.trainListIndex, enabled);
    }

    // FIXME surely this is something?

    /**
     * @param enabled
     */
    public void setStationTabEnabled(boolean enabled) {
        this.setEnabledAt(this.stationInfoIndex, enabled);
    }

}