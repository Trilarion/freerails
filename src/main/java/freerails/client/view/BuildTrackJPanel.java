/*
 * BuildTrackJPanel.java
 *
 * Created on 23 November 2004, 18:01
 */

package freerails.client.view;

import freerails.client.common.ImageManager;
import freerails.client.common.ImageManagerImpl;
import freerails.client.renderer.RenderersRoot;
import freerails.client.ClientConfig;
import freerails.controller.BuildTrackStrategy;
import freerails.controller.ModelRoot;
import freerails.controller.TrackMoveProducer;
import freerails.util.Utils;
import freerails.world.common.Money;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.TrackRule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import static freerails.controller.TrackMoveProducer.BuildMode.*;

/**
 * A JPanel that presents toggle buttons that let the player select the build
 * mode (build track, upgrade track, build station, bulldoze, and info mode) and
 * select the track/bridge/station type to use.
 *
 */
public class BuildTrackJPanel extends javax.swing.JPanel implements ActiveView {

    private static final long serialVersionUID = 3618701915647850036L;

    private final ImageManager imageManager = new ImageManagerImpl(
            ClientConfig.GRAPHICS_PATH);

    private HashMap<TrackRule.TrackCategories, Integer> selectionSet;

    private ModelRoot modelRoot;

    private TrackMoveProducer trackMoveProducer;

    private StationBuildModel stationBuildModel;
    private javax.swing.JToggleButton addTrack;
    private javax.swing.ButtonGroup bridgeButtonGroup;
    private javax.swing.JPanel bridgesJPanel;
    private javax.swing.ButtonGroup buildModeButtonGroup;
    private javax.swing.ButtonGroup stationButtonGroup;
    private javax.swing.JPanel stationsJPanel;
    private javax.swing.ButtonGroup trackButtonGroup;
    private javax.swing.JPanel trackJPanel;
    private javax.swing.ButtonGroup tunnelButtonGroup;
    private javax.swing.JPanel tunnelsJPanel;
    private javax.swing.JToggleButton viewMode;

    /**
     * Creates new form BuildTrackJPanel
     */
    public BuildTrackJPanel() {
        initComponents();
    }

    /**
     *
     * @param mr
     * @param ar
     * @param vl
     * @param al
     */
    public void setup(ModelRoot mr, ActionRoot ar, RenderersRoot vl,
                      ActionListener al) {

        modelRoot = mr;
        stationBuildModel = ar.getStationBuildModel();
        trackMoveProducer = ar.getTrackMoveProducer();
        if (null == trackMoveProducer)
            throw new NullPointerException();

        selectionSet = new HashMap<>();

        trackButtonGroup = new javax.swing.ButtonGroup();
        bridgeButtonGroup = new javax.swing.ButtonGroup();
        stationButtonGroup = new javax.swing.ButtonGroup();
        tunnelButtonGroup = new javax.swing.ButtonGroup();

        // Remove any existing buttons.
        bridgesJPanel.removeAll();
        stationsJPanel.removeAll();
        trackJPanel.removeAll();
        tunnelsJPanel.removeAll();

        // Add the new set of buttons.
        ReadOnlyWorld world = mr.getWorld();

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            JToggleButton toggleButton = new JToggleButton();
            final Integer ruleID = i;
            TrackRule rule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            TrackRule.TrackCategories category = rule.getCategory();
            Money price = null;
            switch (category) {
                case track:
                    trackButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton
                            .addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                    selectionSet
                                            .put(TrackRule.TrackCategories.track,
                                                    ruleID);
                                    setBuildTrackStrategy();
                                }
                            });
                    price = rule.getPrice();
                    trackJPanel.add(toggleButton);

                    break;
                case bridge:
                    bridgeButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton
                            .addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                    selectionSet.put(
                                            TrackRule.TrackCategories.bridge,
                                            ruleID);
                                    setBuildTrackStrategy();
                                }
                            });

                    bridgesJPanel.add(toggleButton);
                    price = rule.getFixedCost();
                    break;
                case tunnel:

                    tunnelButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton
                            .addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                    selectionSet.put(
                                            TrackRule.TrackCategories.tunnel,
                                            ruleID);
                                    setBuildTrackStrategy();

                                }
                            });
                    price = rule.getPrice();
                    tunnelsJPanel.add(toggleButton);
                    break;
                case station:

                    stationButtonGroup.add(toggleButton);

                    toggleButton.setAction(stationBuildModel
                            .getStationChooseAction(ruleID));

                    toggleButton.setIcon(getIcon(rule.getTypeName()));

                    toggleButton
                            .addActionListener(new java.awt.event.ActionListener() {
                                public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                    selectionSet.put(
                                            TrackRule.TrackCategories.station,
                                            ruleID);
                                }
                            });

                    stationsJPanel.add(toggleButton);
                    price = rule.getFixedCost();
                    break;
            }
            toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
            String tooltip = Utils.capitalizeEveryWord(rule.getTypeName())
                    + " $" + price.toString();
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

    /**
     * Calls setFocusable(false) for each button in the button group.
     */
    private void setFocusableFalse(ButtonGroup bg) {
        for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons
                .hasMoreElements(); ) {
            buttons.nextElement().setFocusable(false);
        }
    }

    private void addNoTunnelsButton() {
        JToggleButton toggleButton = new JToggleButton();
        tunnelButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("no_tunnels"));
        toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
        toggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionSet.put(TrackRule.TrackCategories.tunnel, null);
                setBuildTrackStrategy();
            }
        });
        toggleButton.setToolTipText("Don't build tunnels");
        tunnelsJPanel.add(toggleButton);
    }

    private void addNoBridgesButton() {
        JToggleButton toggleButton = new JToggleButton();
        bridgeButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("no_bridges"));
        toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
        toggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionSet.put(TrackRule.TrackCategories.bridge, null);
                setBuildTrackStrategy();
            }
        });
        toggleButton.setToolTipText("Don't build bridges");
        bridgesJPanel.add(toggleButton);
    }

    private ImageIcon getIcon(String typeName) {
        try {

            String relativeFileName = ClientConfig.ICONS_FOLDER_NAME + File.separator + typeName
                    + ClientConfig.ICON_FILE_EXTENSION;
            relativeFileName = relativeFileName.replace(' ', '_');

            Image im = imageManager.getImage(relativeFileName);
            return new ImageIcon(im);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {// GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buildModeButtonGroup = new javax.swing.ButtonGroup();
        trackButtonGroup = new javax.swing.ButtonGroup();
        bridgeButtonGroup = new javax.swing.ButtonGroup();
        stationButtonGroup = new javax.swing.ButtonGroup();
        tunnelButtonGroup = new javax.swing.ButtonGroup();
        JPanel buildModeJPanel = new JPanel();
        addTrack = new javax.swing.JToggleButton();
        JToggleButton upgradeTrack = new JToggleButton();
        JToggleButton addStation = new JToggleButton();
        JToggleButton bulldoze = new JToggleButton();
        viewMode = new javax.swing.JToggleButton();
        trackJPanel = new javax.swing.JPanel();
        JToggleButton viewMode1 = new JToggleButton();
        bridgesJPanel = new javax.swing.JPanel();
        JToggleButton viewMode2 = new JToggleButton();
        tunnelsJPanel = new javax.swing.JPanel();
        JToggleButton viewMode3 = new JToggleButton();
        stationsJPanel = new javax.swing.JPanel();
        JToggleButton viewMode4 = new JToggleButton();
        JPanel spacer = new JPanel();

        setLayout(new java.awt.GridBagLayout());

        setFocusable(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });

        buildModeJPanel.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(addTrack);
        addTrack.setIcon(getIcon("build track"));
        addTrack.setSelected(true);
        addTrack.setToolTipText("Build Track");
        addTrack.setFocusable(false);
        addTrack.setPreferredSize(new java.awt.Dimension(36, 36));
        addTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackActionPerformed(evt);
            }
        });

        buildModeJPanel.add(addTrack);

        buildModeButtonGroup.add(upgradeTrack);
        upgradeTrack.setIcon(getIcon("upgrade track"));
        upgradeTrack.setToolTipText("Upgrade Track");
        upgradeTrack.setFocusable(false);
        upgradeTrack.setPreferredSize(new java.awt.Dimension(36, 36));
        upgradeTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upgradeTrackActionPerformed(evt);
            }
        });

        buildModeJPanel.add(upgradeTrack);

        buildModeButtonGroup.add(addStation);
        addStation.setIcon(getIcon("build stations"));
        addStation.setToolTipText("Build Station");
        addStation.setFocusable(false);
        addStation.setPreferredSize(new java.awt.Dimension(36, 36));
        addStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStationActionPerformed(evt);
            }
        });

        buildModeJPanel.add(addStation);

        buildModeButtonGroup.add(bulldoze);
        bulldoze.setIcon(getIcon("bulldozer"));
        bulldoze.setToolTipText("Remove Track");
        bulldoze.setFocusable(false);
        bulldoze.setPreferredSize(new java.awt.Dimension(36, 36));
        bulldoze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bulldozeActionPerformed(evt);
            }
        });

        buildModeJPanel.add(bulldoze);

        buildModeButtonGroup.add(viewMode);
        viewMode.setIcon(getIcon("eye"));
        viewMode.setToolTipText("Don't build anything");
        viewMode.setFocusable(false);
        viewMode.setPreferredSize(new java.awt.Dimension(36, 36));
        viewMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeActionPerformed(evt);
            }
        });

        buildModeJPanel.add(viewMode);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(buildModeJPanel, gridBagConstraints);

        trackJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT,
                4, 2));

        buildModeButtonGroup.add(viewMode1);
        viewMode1.setIcon(getIcon("turn_off"));
        viewMode1.setPreferredSize(new java.awt.Dimension(36, 36));
        trackJPanel.add(viewMode1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(trackJPanel, gridBagConstraints);

        bridgesJPanel.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode2);
        viewMode2.setIcon(getIcon("turn_off"));
        viewMode2.setPreferredSize(new java.awt.Dimension(36, 36));
        bridgesJPanel.add(viewMode2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(bridgesJPanel, gridBagConstraints);

        tunnelsJPanel.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode3);
        viewMode3.setIcon(getIcon("turn_off"));
        viewMode3.setPreferredSize(new java.awt.Dimension(36, 36));
        tunnelsJPanel.add(viewMode3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(tunnelsJPanel, gridBagConstraints);

        stationsJPanel.setLayout(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(viewMode4);
        viewMode4.setIcon(getIcon("turn_off"));
        viewMode4.setPreferredSize(new java.awt.Dimension(36, 36));
        stationsJPanel.add(viewMode4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(stationsJPanel, gridBagConstraints);

        spacer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0,
                0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacer, gridBagConstraints);

    }// GEN-END:initComponents

    private void formKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_formKeyTyped
        viewMode.doClick();
    }// GEN-LAST:event_formKeyTyped

    private void formKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_formKeyPressed
        viewMode.doClick();
    }// GEN-LAST:event_formKeyPressed

    private void viewModeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_viewModeActionPerformed
        setVisible(false, false, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(IGNORE_TRACK);
    }// GEN-LAST:event_viewModeActionPerformed

    private void bulldozeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_bulldozeActionPerformed

        setVisible(false, false, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(REMOVE_TRACK);
    }// GEN-LAST:event_bulldozeActionPerformed

    private void addStationActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addStationActionPerformed

        setVisible(false, false, false, true);
        setTrackBuilderMode(BUILD_STATION);

    }// GEN-LAST:event_addStationActionPerformed

    private void upgradeTrackActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_upgradeTrackActionPerformed

        setVisible(true, true, false, false);
        cancelStationPlacement();
        setTrackBuilderMode(UPGRADE_TRACK);

    }// GEN-LAST:event_upgradeTrackActionPerformed

    private void addTrackActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_addTrackActionPerformed

        setVisible(true, true, true, false);
        cancelStationPlacement();
        setTrackBuilderMode(BUILD_TRACK);
    }// GEN-LAST:event_addTrackActionPerformed

    private void setVisible(boolean track, boolean bridges, boolean tunnels,
                            boolean stations) {
        trackJPanel.setVisible(bridges);
        bridgesJPanel.setVisible(bridges);
        tunnelsJPanel.setVisible(tunnels);
        stationsJPanel.setVisible(stations);
    }

    private void setBuildTrackStrategy() {
        ArrayList<Integer> ruleIDs = new ArrayList<>();
        ruleIDs.add(selectionSet.get(TrackRule.TrackCategories.track));
        ruleIDs.add(selectionSet.get(TrackRule.TrackCategories.bridge));
        ruleIDs.add(selectionSet.get(TrackRule.TrackCategories.tunnel));
        BuildTrackStrategy bts = BuildTrackStrategy.getMultipleRuleInstance(
                ruleIDs, modelRoot.getWorld());
        modelRoot.setProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY, bts);
    }

    private void cancelStationPlacement() {
        // Cancel build station mode..
        stationBuildModel.getStationCancelAction().actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    }

    private void setTrackBuilderMode(TrackMoveProducer.BuildMode mode) {
        trackMoveProducer.setTrackBuilderMode(mode);
        modelRoot.setProperty(ModelRoot.Property.TRACK_BUILDER_MODE, mode);
    }

    // End of variables declaration                   

}
