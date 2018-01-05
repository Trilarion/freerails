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
 * TrainSummaryJPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.*;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;

import javax.swing.*;
import java.awt.*;

/**
 */
public class TrainSummaryJPanel extends javax.swing.JPanel implements
        ListCellRenderer, View {

    private static final long serialVersionUID = 4121133628006020919L;
    private final TrainSummeryModel model;
    private final Color backgoundColor = (java.awt.Color) javax.swing.UIManager
            .getDefaults().get("List.background");
    private final Color selectedColor = (java.awt.Color) javax.swing.UIManager
            .getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private ReadOnlyWorld w;
    private FreerailsPrincipal principal;
    private TrainListCellRenderer trainListCellRenderer1;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel headingLabel;
    private javax.swing.JLabel trainIncomeLabel;
    private javax.swing.JLabel trainMaintenanceCostLabel;
    private javax.swing.JLabel trainNumLabel;

    /**
     * Creates new form TrainSummaryJPanel
     */
    public TrainSummaryJPanel() {
        model = new TrainSummeryModel();
        initComponents();
    }

    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        this.principal = modelRoot.getPrincipal();
        this.w = modelRoot.getWorld();
        trainListCellRenderer1 = new TrainListCellRenderer(modelRoot, vl);
        trainListCellRenderer1.setHeight(15);
        model.setWorld(w, principal);
    }

    private String findStationName(int trainNum) {
        return model.getStationName(trainNum);
    }

    private String findTrainIncome(int trainNum) {
        Money m = model.findTrainIncome(trainNum);
        return '$' + m.toString();
    }

    public java.awt.Component getListCellRendererComponent(
            javax.swing.JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        int trainID = NonNullElementWorldIterator
                .row2index(w, KEY.TRAINS, principal, index);
        String trainNumText = "#" + (trainID + 1);

        trainNumLabel.setText(trainNumText);
        headingLabel.setText(findStationName(trainID));
        trainMaintenanceCostLabel.setText(findMaintenanceCost());
        trainIncomeLabel.setText(findTrainIncome(trainID));

        java.awt.GridBagConstraints gridBagConstraints;

        trainListCellRenderer1.setOpaque(true);
        trainListCellRenderer1.setCenterTrain(false);
        trainListCellRenderer1.display(trainID);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(trainListCellRenderer1, gridBagConstraints);

        if (isSelected) {
            if (list.isFocusOwner()) {
                setBackground(selectedColor);
                trainListCellRenderer1.setBackground(selectedColor);
            } else {
                setBackground(selectedColorNotFocused);
                trainListCellRenderer1.setBackground(selectedColorNotFocused);
            }
        } else {
            setBackground(backgoundColor);
            trainListCellRenderer1.setBackground(backgoundColor);
        }
        // Set selected
        return this;
    }

    private String findMaintenanceCost() {

        GameTime time = w.currentTime();
        GameCalendar gameCalendar = (GameCalendar) w.get(ITEM.CALENDAR);
        double month = gameCalendar.getMonth(time.getTicks());
        long cost = (long) (month / 12 * 5000);

        Money m = new Money(cost);
        return '$' + m.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {// GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        trainNumLabel = new javax.swing.JLabel();
        headingLabel = new javax.swing.JLabel();
        trainMaintenanceCostLabel = new javax.swing.JLabel();
        trainIncomeLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 50));
        trainNumLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainNumLabel.setText("jLabel1");
        trainNumLabel
                .setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        trainNumLabel.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(trainNumLabel, gridBagConstraints);

        headingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        headingLabel.setText("jLabel2");
        headingLabel
                .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        headingLabel.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(headingLabel, gridBagConstraints);

        trainMaintenanceCostLabel
                .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainMaintenanceCostLabel.setText("jLabel3");
        trainMaintenanceCostLabel.setMaximumSize(getMaximumSize());
        trainMaintenanceCostLabel.setPreferredSize(new java.awt.Dimension(100,
                25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(trainMaintenanceCostLabel, gridBagConstraints);

        trainIncomeLabel
                .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainIncomeLabel.setText("jLabel1");
        trainIncomeLabel.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(trainIncomeLabel, gridBagConstraints);

    }// GEN-END:initComponents
    // End of variables declaration//GEN-END:variables

}
