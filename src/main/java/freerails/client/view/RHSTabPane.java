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

import freerails.client.*;
import freerails.client.renderer.RendererRoot;
import freerails.util.Vector2D;
import freerails.model.station.Station;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.FullTerrainTile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * The tabbed panel that sits in the lower right hand corner of the screen.
 */
public class RHSTabPane extends JTabbedPane implements ModelRootListener {

    private static final long serialVersionUID = 3906926798502965297L;
    private static final Logger LOGGER = Logger.getLogger(RHSTabPane.class.getName());
    private final TerrainInfoPanel terrainInfoPanel;
    private final StationInfoPanel stationInfoPanel;
    private final TrainListPanel trainListPanel;
    private final BuildTrackPanel buildTrackPanel;
    private final int terrainInfoIndex;
    private final int trainListIndex;
    private final int stationInfoIndex;
    private ReadOnlyWorld world;
    private ModelRoot modelRoot;

    /**
     *
     */
    public RHSTabPane() {
        /*
         * Don't accept keyboard focus since we want to leave it with the main
         * map view.
         */
        setFocusable(false);

        ImageIcon trainListIcon;
        ImageIcon buildTrackIcon;
        ImageIcon stationInfoIcon;

        // set up trainsJTabbedPane
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        terrainInfoPanel = new TerrainInfoPanel();

        trainListPanel = new TrainListPanel(true);

        buildTrackPanel = new BuildTrackPanel();
        trainListPanel.removeButtons();

        URL terrainInfoIconUrl = getClass().getResource(ClientConfig.ICON_TERRAIN_INFO);
        Icon terrainInfoIcon = new ImageIcon(terrainInfoIconUrl);

        URL buildTrackIconUrl = getClass().getResource(ClientConfig.ICON_NEW_TRACK);
        buildTrackIcon = new ImageIcon(buildTrackIconUrl);

        URL trainListIconUrl = getClass().getResource(ClientConfig.ICON_TRAIN_LIST);
        trainListIcon = new ImageIcon(trainListIconUrl);

        URL stationListIconUrl = getClass().getResource(ClientConfig.ICON_STATION_LIST);
        stationInfoIcon = new ImageIcon(stationListIconUrl);

        // Note titles set to null so only the icon appears at the top of the
        // top.
        JScrollPane terrainInfoJScrollPane = new JScrollPane(terrainInfoPanel);
        terrainInfoJScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(null, terrainInfoIcon, terrainInfoJScrollPane, "Terrain Info");
        terrainInfoIndex = getTabCount() - 1;

        stationInfoPanel = new StationInfoPanel();
        stationInfoPanel.removeCloseButton();
        // Don't show the station info tab until it has been rewritten to take
        // up less space.
        JScrollPane stationInfoJScrollPane = new JScrollPane(stationInfoPanel);
        stationInfoJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(null, stationInfoIcon, stationInfoJScrollPane, "Station Info");
        stationInfoIndex = getTabCount() - 1;

        trainListPanel.setTrainViewHeight(20);
        addTab(null, buildTrackIcon, buildTrackPanel, "Build Track");
        addTab(null, trainListIcon, trainListPanel, "Train List");
        trainListIndex = getTabCount() - 1;

        // These values were picked by trial and error!
        setMinimumSize(new Dimension(250, 200));
    }

    /**
     * @param actionRoot
     * @param vl
     * @param modelRoot
     */
    public void setup(final ActionRoot actionRoot, RendererRoot vl, final ModelRootImpl modelRoot) {

        this.modelRoot = modelRoot;
        world = modelRoot.getWorld();

        terrainInfoPanel.setup(world, vl);
        stationInfoPanel.setup(modelRoot, vl, null);

        ActionListener showTrain = e -> {
            int id = trainListPanel.getSelectedTrainID();
            actionRoot.getDialogueBoxController().showTrainOrders(id);
        };

        trainListPanel.setShowTrainDetailsActionListener(showTrain);
        trainListPanel.setup(modelRoot, vl, null);
        modelRoot.addPropertyChangeListener(this);

        buildTrackPanel.setup(modelRoot, actionRoot);
    }

    /**
     * Updates the Terrain Info Panel if the specified PropertyChangeEvent was
     * triggered by the cursor moving.
     */
    public void propertyChange(ModelRootProperty modelRootProperty, Object before, Object after) {
        if (modelRootProperty == ModelRootProperty.CURSOR_POSITION) {

            Vector2D location = (Vector2D) after;

            // Select priority element at location
            LOGGER.debug("Let's try to show the station.");

            // select station at point and show stat info tab
            // if not, then do terrain info and show that
            int stationNumberAtLocation = Station.getStationNumberAtLocation(world, modelRoot.getPrincipal(), location);
            if (stationNumberAtLocation > -1) {
                stationInfoPanel.setStation(stationNumberAtLocation);
                setSelectedIndex(stationInfoIndex);
            } else {
                //terrainInfoPanel.showTerrainInfo(x, y);
                terrainInfoPanel.setTerrainType(((FullTerrainTile) world.getTile(location)).getTerrainTypeID());
                setSelectedIndex(terrainInfoIndex);
            }
        }
    }

    /**
     * @param enabled
     */
    public void setTrainTabEnabled(boolean enabled) {
        setEnabledAt(trainListIndex, enabled);
    }

    /**
     * @param enabled
     */
    public void setStationTabEnabled(boolean enabled) {
        setEnabledAt(stationInfoIndex, enabled);
    }

}