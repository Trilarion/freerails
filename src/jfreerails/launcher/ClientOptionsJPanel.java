/*
 * ClientOptionsJPanel.java
 *
 * Created on 20 December 2003, 15:57
 */

package jfreerails.launcher;

import java.awt.DisplayMode;

import jfreerails.client.view.DisplayModesComboBoxModels;
import jfreerails.client.view.MyDisplayMode;

/**
 *
 * @author  rtuck99@users.sourceforge.net
 */
class ClientOptionsJPanel extends javax.swing.JPanel {
    private Launcher owner;
    
    String getPlayerName() {
	return playerName.getText();
    }

    DisplayMode getDisplayMode() {
	return ((MyDisplayMode) listModel.getSelectedItem()).displayMode;
    }

    boolean isWindowed() {
	return windowedButton.isSelected();
    }

    public void setControlsEnabled(boolean enabled) {
	windowedButton.setEnabled(enabled);
	fullScreenButton.setEnabled(enabled);
	if (fullScreenButton.isSelected()) {
	    jList1.setEnabled(enabled);
	}
    }
    
    private void validateSettings() {
	boolean isValid = false;
	String infoText = "";
	if (playerName.getText() == null ||
	    playerName.getText().equals("")) {
	    infoText = "Please set a name for your player";
	} else {
	    isValid = true;
	}
	owner.setInfoText(infoText);
	owner.setNextEnabled(isValid);
    }

    private DisplayModesComboBoxModels listModel;

    /** Creates new form ClientOptionsJPanel */
    public ClientOptionsJPanel(Launcher owner) {
	this.owner = owner;
        initComponents();
	listModel = new DisplayModesComboBoxModels();
	jList1.setModel(listModel);
	jList1.setSelectedIndex(0);
	validateSettings();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        playerName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        windowedButton = new javax.swing.JRadioButton();
        fullScreenButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.BorderLayout());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel3.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Player Details"));
        jLabel1.setText("Player name:");
        jPanel3.add(jLabel1);

        playerName.setColumns(12);
        playerName.setText("Player1");
        playerName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playerNameActionPerformed(evt);
            }
        });
        playerName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                playerNameFocusLost(evt);
            }
        });

        jPanel3.add(playerName);

        add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Select Display Mode"));
        jScrollPane1.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setEnabled(false);
        jScrollPane1.setViewportView(jList1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        windowedButton.setSelected(true);
        windowedButton.setText("Windowed");
        buttonGroup1.add(windowedButton);
        jPanel2.add(windowedButton);

        fullScreenButton.setText("Full screen");
        buttonGroup1.add(fullScreenButton);
        fullScreenButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fullScreenButtonStateChanged(evt);
            }
        });

        jPanel2.add(fullScreenButton);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
	validateSettings();
    }//GEN-LAST:event_formComponentShown

    private void playerNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playerNameActionPerformed
	validateSettings();
    }//GEN-LAST:event_playerNameActionPerformed

    private void playerNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_playerNameFocusLost
	validateSettings();
    }//GEN-LAST:event_playerNameFocusLost

    private void fullScreenButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fullScreenButtonStateChanged
	jList1.setEnabled(fullScreenButton.isSelected());
    }//GEN-LAST:event_fullScreenButtonStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton fullScreenButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField playerName;
    private javax.swing.JRadioButton windowedButton;
    // End of variables declaration//GEN-END:variables
    
}