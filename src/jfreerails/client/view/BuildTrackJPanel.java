/*
 * BuildTrackJPanel.java
 *
 * Created on 23 November 2004, 18:01
 */

package jfreerails.client.view;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageManagerImpl;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackRule;
import java.util.HashMap;

/**
 * A JPanel that presents toggle buttons that let the player select the build mode (build track, upgrade track, build
 * station, bulloze, and info mode) and select the track/bridge/station type to use.
 * @author  Luke
 */
public class BuildTrackJPanel extends javax.swing.JPanel implements View {
    
    private final ImageManager imageManager = new ImageManagerImpl("/jfreerails/client/graphics/");
    
    /** Creates new form BuildTrackJPanel */
    public BuildTrackJPanel() {
        initComponents();
    }
    
    public void setup(ModelRoot mr, ViewLists vl, ActionListener al){
        
        HashMap<TrackRule.TrackCategories, TrackRule> selectionSet = new HashMap<TrackRule.TrackCategories, TrackRule>();
        
        //Remove any existing buttons.
        
        trackButtonGroup = new javax.swing.ButtonGroup();
        bridgeButtonGroup = new javax.swing.ButtonGroup();
        stationButtonGroup = new javax.swing.ButtonGroup();
        tunnelButtonGroup = new javax.swing.ButtonGroup();
        
        bridgesJPanel.removeAll();
        stationsJPanel.removeAll();
        trackJPanel.removeAll();
        tunnelsJPanel.removeAll();
        
        //Add the new set of buttons.
        ReadOnlyWorld ro = mr.getWorld();
        for(int i = 0; i < ro.size(SKEY.TRACK_RULES); i++){
            JToggleButton toggleButton = new JToggleButton();
            TrackRule rule = (TrackRule)ro.get(SKEY.TRACK_RULES, i);
            TrackRule.TrackCategories category = rule.getCategory();
            switch (category.ordinal()){
                case 0:
                    trackButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            
                        }
                    });
                    
                    trackJPanel.add(toggleButton);
                    
                    break;
                case 1:
                    bridgeButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            
                        }
                    });
                    
                    bridgesJPanel.add(toggleButton);
                    break;
                case  2:
                    
                    tunnelButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    toggleButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            
                        }
                    });
                    
                    tunnelsJPanel.add(toggleButton);
                    break;
                case 3:
                    
                    stationButtonGroup.add(toggleButton);
                    toggleButton.setIcon(getIcon(rule.getTypeName()));
                    
                    toggleButton.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            
                        }
                    });
                    
                    stationsJPanel.add(toggleButton);
                    break;
            }
            toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
            if(!selectionSet.containsKey(category)){
                selectionSet.put(category, rule);
                toggleButton.setSelected(true);
            }
            
        }
        addNoTunnelsButton();
        addNoBridgesButton();
        
        //Default to add track.
        addTrackActionPerformed(null);
    }
    
    private void addNoTunnelsButton(){
        JToggleButton toggleButton = new JToggleButton();
        tunnelButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("turn_off"));
        toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
        toggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
            }
        });
        
        tunnelsJPanel.add(toggleButton);
    }
    
    private void addNoBridgesButton(){
        JToggleButton toggleButton = new JToggleButton();
        bridgeButtonGroup.add(toggleButton);
        toggleButton.setIcon(getIcon("turn_off"));
        toggleButton.setPreferredSize(new java.awt.Dimension(36, 36));
        toggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
            }
        });
        
        bridgesJPanel.add(toggleButton);
    }
    
    
    
    private ImageIcon getIcon(String typeName) {
        try {
            
            String relativeFileName = "icons" + File.separator +
                    typeName+".png";
            relativeFileName = relativeFileName.replace(' ', '_');
            
            Image im = imageManager.getImage(relativeFileName);
            return new ImageIcon(im);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        buildModeButtonGroup = new javax.swing.ButtonGroup();
        trackButtonGroup = new javax.swing.ButtonGroup();
        bridgeButtonGroup = new javax.swing.ButtonGroup();
        stationButtonGroup = new javax.swing.ButtonGroup();
        tunnelButtonGroup = new javax.swing.ButtonGroup();
        buildModeJPanel = new javax.swing.JPanel();
        addTrack = new javax.swing.JToggleButton();
        upgradeTrack = new javax.swing.JToggleButton();
        addStation = new javax.swing.JToggleButton();
        bulldoze = new javax.swing.JToggleButton();
        viewMode = new javax.swing.JToggleButton();
        trackJPanel = new javax.swing.JPanel();
        viewMode1 = new javax.swing.JToggleButton();
        bridgesJPanel = new javax.swing.JPanel();
        viewMode2 = new javax.swing.JToggleButton();
        tunnelsJPanel = new javax.swing.JPanel();
        viewMode3 = new javax.swing.JToggleButton();
        stationsJPanel = new javax.swing.JPanel();
        viewMode4 = new javax.swing.JToggleButton();
        spacer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        buildModeJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));

        buildModeButtonGroup.add(addTrack);
        addTrack.setIcon(getIcon("build track"));
        addTrack.setSelected(true);
        addTrack.setPreferredSize(new java.awt.Dimension(36, 36));
        addTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackActionPerformed(evt);
            }
        });

        buildModeJPanel.add(addTrack);

        buildModeButtonGroup.add(upgradeTrack);
        upgradeTrack.setIcon(getIcon("upgrade track"));
        upgradeTrack.setPreferredSize(new java.awt.Dimension(36, 36));
        upgradeTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upgradeTrackActionPerformed(evt);
            }
        });

        buildModeJPanel.add(upgradeTrack);

        buildModeButtonGroup.add(addStation);
        addStation.setIcon(getIcon("build stations"));
        addStation.setPreferredSize(new java.awt.Dimension(36, 36));
        addStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStationActionPerformed(evt);
            }
        });

        buildModeJPanel.add(addStation);

        buildModeButtonGroup.add(bulldoze);
        bulldoze.setIcon(getIcon("bulldozer"));
        bulldoze.setPreferredSize(new java.awt.Dimension(36, 36));
        bulldoze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bulldozeActionPerformed(evt);
            }
        });

        buildModeJPanel.add(bulldoze);

        buildModeButtonGroup.add(viewMode);
        viewMode.setIcon(getIcon("turn_off"));
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

        trackJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));

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

        bridgesJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));

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

        tunnelsJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));

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

        stationsJPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));

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

        spacer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacer, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void viewModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeActionPerformed
        // TODO add your handling code here:
        setVisible(false, false, false, false);
    }//GEN-LAST:event_viewModeActionPerformed
    
    private void bulldozeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bulldozeActionPerformed
        // TODO add your handling code here:
        setVisible(false, false, false, false);
    }//GEN-LAST:event_bulldozeActionPerformed
    
    private void addStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStationActionPerformed
        // TODO add your handling code here:
        setVisible(false, false, false, true);
        
    }//GEN-LAST:event_addStationActionPerformed
    
    private void upgradeTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upgradeTrackActionPerformed
        // TODO add your handling code here:
        setVisible(true, true, false, false);
    }//GEN-LAST:event_upgradeTrackActionPerformed
    
    private void addTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackActionPerformed
        // TODO add your handling code here:
        setVisible(true, true, true, false);
    }//GEN-LAST:event_addTrackActionPerformed
    
    private void setVisible(boolean track, boolean bridges, boolean tunnels, boolean stations){
        trackJPanel.setVisible(bridges);
        bridgesJPanel.setVisible(bridges);
        tunnelsJPanel.setVisible(tunnels);
        stationsJPanel.setVisible(stations);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JToggleButton addStation;
    javax.swing.JToggleButton addTrack;
    javax.swing.ButtonGroup bridgeButtonGroup;
    javax.swing.JPanel bridgesJPanel;
    javax.swing.ButtonGroup buildModeButtonGroup;
    javax.swing.JPanel buildModeJPanel;
    javax.swing.JToggleButton bulldoze;
    javax.swing.JPanel spacer;
    javax.swing.ButtonGroup stationButtonGroup;
    javax.swing.JPanel stationsJPanel;
    javax.swing.ButtonGroup trackButtonGroup;
    javax.swing.JPanel trackJPanel;
    javax.swing.ButtonGroup tunnelButtonGroup;
    javax.swing.JPanel tunnelsJPanel;
    javax.swing.JToggleButton upgradeTrack;
    javax.swing.JToggleButton viewMode;
    javax.swing.JToggleButton viewMode1;
    javax.swing.JToggleButton viewMode2;
    javax.swing.JToggleButton viewMode3;
    javax.swing.JToggleButton viewMode4;
    // End of variables declaration//GEN-END:variables
    
}
