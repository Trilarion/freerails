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
 * BuildTrackPanel.java
 *
 */

package freerails.client.view;

import freerails.client.ActionRoot;
import freerails.client.ClientConfig;
import freerails.client.ModelRootProperty;
import freerails.util.ui.ImageManager;
import freerails.util.ui.ImageManagerImpl;
import freerails.client.model.StationBuildModel;
import freerails.controller.BuildMode;
import freerails.model.track.BuildTrackStrategy;
import freerails.client.ModelRoot;
import freerails.controller.TrackMoveProducer;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.finances.Money;
import freerails.model.track.TrackCategories;
import freerails.model.track.TrackRule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Presents toggle buttons that let the player select the build
 * mode (build track, upgrade track, build station, bulldoze, and info mode) and
 * select the track/bridge/station type to use.
 */
public class BuildTrackPanel extends JPanel implements ActiveView {

    private static final long serialVersionUID = 3618701915647850036L;
    private final ImageManager imageManager = new ImageManagerImpl(ClientConfig.GRAPHICS_PATH);
    private HashMap<TrackCategories, Integer> selectionSet;
    private ModelRoot modelRoot;
    private TrackMoveProducer trackMoveProducer;
    private StationBuildModel stationBuildModel;
    private JToggleButton addTrack;
    private ButtonGroup bridgeButtonGroup;
    private JPanel bridgesJPanel;
    private ButtonGroup buildModeButtonGroup;
    private ButtonGroup stationButtonGroup;
    private JPanel stationsJPanel;
    private ButtonGroup trackButtonGroup;
    private JPanel trackJPanel;
    private ButtonGroup tunnelButtonGroup;
    private JPanel tunnelsJPanel;
    private JToggleButton viewMode;

    /**
     * Creates new form BuildTrackPanel
     */
    public BuildTrackPanel() {
        GridBagConstraints gridBagConstraints;

        buildModeButtonGroup = new ButtonGroup();
        trackButtonGroup = new ButtonGroup();
        bridgeButtonGroup = new ButtonGroup();
        stationButtonGroup = new ButtonGroup();
        tunnelButtonGroup = new ButtonGroup();
        JPanel buildModeJPanel = new JPanel();
        addTrack = new JToggleButton();
        JToggleButton upgradeTrack = new JToggleButton();
        JToggleButton addStation = new JToggleButton();
        JToggleButton bulldoze = new JToggleButton();
        viewMode = new JToggleButton();
        trackJPanel = new JPanel();
        JToggleButton viewMode1 = new JToggleButton();
        bridgesJPanel = new JPanel();
        JToggleButton viewMode2 = new JToggleButton();
        tunnelsJPanel = new JPanel();
        JToggleButton viewMode3 = new JToggleButton();
        stationsJPanel = new JPanel();
        JToggleButton viewMode4 = new JToggleButton();
        JPanel spacer = new JPanel();

        setLayout(new GridBagLayout());

        setFocusable(false);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                formKeyPressed(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                formKeyTyped(e);
            }
        });

        buildModeJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(addTrack);
        addTrack.setIcon(getIcon("build track"));
        addTrack.setSelected(true);
        addTrack.setToolTipText("Build Track");
        addTrack.setFocusable(false);
        addTrack.setPreferredSize(new Dimension(36, 36));
        addTrack.addActionListener(this::addTrackActionPerformed);

        buildModeJPanel.add(addTrack);

        buildModeButtonGroup.add(upgradeTrack);
        upgradeTrack.setIcon(getIcon("upgrade track"));
        upgradeTrack.setToolTipText("Upgrade Track");
        upgradeTrack.setFocusable(false);
        upgradeTrack.setPreferredSize(new Dimension(36, 36));
        upgradeTrack.addActionListener(this::upgradeTrackActionPerformed);

        buildModeJPanel.add(upgradeTrack);

        buildModeButtonGroup.add(addStation);
        addStation.setIcon(getIcon("build stations"));
        addStation.setToolTipText("Build Station");
        addStation.setFocusable(false);
        addStation.setPreferredSize(new Dimension(36, 36));
        addStation.addActionListener(this::addStationActionPerformed);

        buildModeJPanel.add(addStation);

        buildModeButtonGroup.add(bulldoze);
        bulldoze.setIcon(getIcon("bulldozer"));
        bulldoze.setToolTipText("Remove Track");
        bulldoze.setFocusable(false);
        bulldoze.setPreferredSize(new Dimension(36, 36));
        bulldoze.addActionListener(this::bulldozeActionPerformed);

        buildModeJPanel.add(bulldoze);

        buildModeButtonGroup.add(viewMode);
        viewMode.setIcon(getIcon("eye"));
        viewMode.setToolTipText("Don't build anything");
        viewMode.setFocusable(false);
        viewMode.setPreferredSize(new Dimension(36, 36));
        viewMode.addActionListener(this::viewModeActionPerformed);

        buildModeJPanel.add(viewMode);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(buildModeJPanel, gridBagConstraints);

        trackJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode1);
        viewMode1.setIcon(getIcon("turn_off"));
        viewMode1.setPreferredSize(new Dimension(36, 36));
        trackJPanel.add(viewMode1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(trackJPanel, gridBagConstraints);

        bridgesJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode2);
        viewMode2.setIcon(getIcon("turn_off"));
        viewMode2.setPreferredSize(new Dimension(36, 36));
        bridgesJPanel.add(viewMode2);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(bridgesJPanel, gridBagConstraints);

        tunnelsJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode3);
        viewMode3.setIcon(getIcon("turn_off"));
        viewMode3.setPreferredSize(new Dimension(36, 36));
        tunnelsJPanel.add(viewMode3);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(tunnelsJPanel, gridBagConstraints);

        stationsJPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode4);
        viewMode4.setIcon(getIcon("turn_off"));
        viewMode4.setPreferredSize(new Dimension(36, 36));
        stationsJPanel.add(viewMode4);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        add(stationsJPanel, gridBagConstraints);

        spacer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacer, gridBagConstraints);
    }

    /**
     * Calls setFocusable(false) for each button in the button group.
     */
    private static void setFocusableFalse(ButtonGroup bg) {
        for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements(); ) {
            buttons.nextElement().setFocusable(false);
        }
    }

    /**
     * @param modelRoot
     * @param actionRoot
     */
    public void setup(ModelRoot modelRoot, ActionRoot actionRoot) {

        this.modelRoot = modelRoot;
        stationBuildModel = actionRoot.getStationBuildModel();
        trackMoveProducer = actionRoot.getTrackMoveProducer();
        Utils.verifyNotNull(trackMoveProducer);

        selectionSet = new HashMap<>();

        trackButtonGroup = new ButtonGroup();
        bridgeButtonGroup = new ButtonGroup();
        stationButtonGroup = new ButtonGroup();
        tunnelButtonGroup = new ButtonGroup();

        // Remove any existing buttons.
        bridgesJPanel.removeAll();
        stationsJPanel.removeAll();
        trackJPanel.removeAll();
        tunnelsJPanel.removeAll();

        // Add the new set of buttons.
        ReadOnlyWorld world = modelRoot.getWorld();

        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            JToggleButton toggleButton = new JToggleButton();
            final Integer ruleID = i;
            TrackRule rule = (TrackRule) world.get(SharedKey.TrackRules, i);
            TrackCategories category = rule.getCategory();
            Money price = null;
            switch (category) {
                case track:
                    trackButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(e -> {
                        selectionSet.put(TrackCategories.track, ruleID);
                        setBuildTrackStrategy();
                    });
                    price = rule.getPrice();
                    trackJPanel.add(toggleButton);

                    break;
                case bridge:
                    bridgeButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(e -> {
                        selectionSet.put(TrackCategories.bridge, ruleID);
                        setBuildTrackStrategy();
                    });

                    bridgesJPanel.add(toggleButton);
                    price = rule.getFixedCost();
                    break;
                case tunnel:

                    tunnelButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(e -> {
                        selectionSet.put(TrackCategories.tunnel, ruleID);
                        setBuildTrackStrategy();
                    });
                    price = rule.getPrice();
                    tunnelsJPanel.add(toggleButton);
                    break;
                case station:

                    stationButtonGroup.add(toggleButton);

                    toggleButton.setAction(stationBuildModel.getStationChooseAction(ruleID));

                    toggleButton.setIcon(getIcon(rule.getTypeName()));

                    toggleButton.addActionListener(e -> selectionSet.put(TrackCategories.station, ruleID));

                    stationsJPanel.add(toggleButton);
                    price = rule.getFixedCost();
                    break;
            }
            toggleButton.setPreferredSize(new Dimension(36, 36));
            String tooltip = rule.getTypeName() + " $" + price.toString();
            toggleButton.setToolTipText(tooltip);
            if (!selectionSet.containsKey(category)) {
                selectionSet.put(category, i);
                toggleButton.setSelected(true);
            }
        }
        addNoTunnelsButton();
        addNoBridgesButton();

        // Default to add track.
        addTrackActionPerformed(null);
        buildModeButtonGroup.setSelected(addTrack.getModel(), true);
        setBuildTrackStrategy();

        // Make the buttons non-focusable
        setFocusableFalse(bridgeButtonGroup);
        setFocusableFalse(trackButtonGroup);
        setFocusableFalse(tunnelButtonGroup);
        setFocusableFalse(stationButtonGroup);
        setFocusableFalse(buildModeButtonGroup);

        // Add button click
        // buildTrackJPanel.addKeyListener(new KeyListener(){
        // public void keyPressed(KeyEvent e){
        // System.out.println(e.getKeyCode());
        // viewMode.doClick();
        // }
        // public void keyReleased(KeyEvent e){
        //
        // }
        // public void keyTyped(KeyEvent e){
        //
        // }
        // });
    }

    private void addNoTunnelsButton() {
        JToggleButton toggleButton = new JToggleButton();
        tunnelButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("no_tunnels"));
        toggleButton.setPreferredSize(new Dimension(36, 36));
        toggleButton.addActionListener(e -> {
            selectionSet.put(TrackCategories.tunnel, null);
            setBuildTrackStrategy();
        });
        toggleButton.setToolTipText("Don't build tunnels");
        tunnelsJPanel.add(toggleButton);
    }

    private void addNoBridgesButton() {
        JToggleButton toggleButton = new JToggleButton();
        bridgeButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("no_bridges"));
        toggleButton.setPreferredSize(new Dimension(36, 36));
        toggleButton.addActionListener(e -> {
            selectionSet.put(TrackCategories.bridge, null);
            setBuildTrackStrategy();
        });
        toggleButton.setToolTipText("Don't build bridges");
        bridgesJPanel.add(toggleButton);
    }

    private Icon getIcon(String typeName) {
        try {

            String relativeFileName = ClientConfig.ICONS_FOLDER_NAME + File.separator + typeName + ClientConfig.ICON_FILE_EXTENSION;
            relativeFileName = relativeFileName.replace(' ', '_');

            Image im = imageManager.getImage(relativeFileName);
            return new ImageIcon(im);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    private void formKeyTyped(KeyEvent evt) {
        viewMode.doClick();
    }

    private void formKeyPressed(KeyEvent evt) {
        viewMode.doClick();
    }

    private void viewModeActionPerformed(ActionEvent evt) {
        setVisible(false, false, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(BuildMode.IGNORE_TRACK);
    }

    private void bulldozeActionPerformed(ActionEvent evt) {

        setVisible(false, false, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(BuildMode.REMOVE_TRACK);
    }

    private void addStationActionPerformed(ActionEvent evt) {

        setVisible(false, false, false, true);
        setTrackBuilderMode(BuildMode.BUILD_STATION);
    }

    private void upgradeTrackActionPerformed(ActionEvent evt) {

        setVisible(true, true, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(BuildMode.UPGRADE_TRACK);
    }

    private void addTrackActionPerformed(ActionEvent evt) {

        setVisible(true, true, true, false);
        cancelStationPlacement();
        setTrackBuilderMode(BuildMode.BUILD_TRACK);
    }

    private void setVisible(boolean track, boolean bridges, boolean tunnels, boolean stations) {
        trackJPanel.setVisible(bridges);
        bridgesJPanel.setVisible(bridges);
        tunnelsJPanel.setVisible(tunnels);
        stationsJPanel.setVisible(stations);
    }

    private void setBuildTrackStrategy() {
        Collection<Integer> ruleIDs = new ArrayList<>();
        ruleIDs.add(selectionSet.get(TrackCategories.track));
        ruleIDs.add(selectionSet.get(TrackCategories.bridge));
        ruleIDs.add(selectionSet.get(TrackCategories.tunnel));
        BuildTrackStrategy bts = BuildTrackStrategy.getMultipleRuleInstance(ruleIDs, modelRoot.getWorld());
        modelRoot.setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, bts);
    }

    private void cancelStationPlacement() {
        // Cancel build station mode..
        stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    }

    private void setTrackBuilderMode(BuildMode mode) {
        trackMoveProducer.setTrackBuilderMode(mode);
        modelRoot.setProperty(ModelRootProperty.TRACK_BUILDER_MODE, mode);
    }


}
