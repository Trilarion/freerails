/*
 * Copyright (C) 2004 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * StationJPanel.java
 *
 * Created on 22 October 2004, 00:06
 */
package org.railz.client.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.railz.client.model.*;
import org.railz.client.renderer.*;
import org.railz.move.*;
import org.railz.util.*;
import org.railz.world.cargo.*;
import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;

/**
 * Displays info about station improvements and enables purchase of new
 * imrpovements.
 *
 * @author  bob
 */
class StationJPanel extends javax.swing.JPanel {
    private ModelRoot modelRoot;
    private ObjectKey stationKey;
    private World2ListModelAdapter w2lma;
    private static final int ICON_HEIGHT = 12;

    private void updateButtonInfo(int improvementId) {
	StationImprovement si = (StationImprovement) modelRoot.getWorld()
	    .get(KEY.STATION_IMPROVEMENTS, improvementId,
		    Player.AUTHORITATIVE);
	impNameJLabel.setText(Resources.get(si.getName()));
	impDescrJTextArea.setText(Resources.get(si.getDescription()));
	StationModelViewer smv = new StationModelViewer(modelRoot.getWorld());
	StationModel sm = (StationModel) modelRoot.getWorld().get(KEY.STATIONS,
		stationKey.index, stationKey.principal);
	smv.setStationModel(sm);
	impCostJLabel.setText("$" + smv.getImprovementCost(improvementId));
	purchaseInfoJPanel.repaint();
    }

    private class ImprovementJButton extends JButton {
	private final int id;

	private MouseListener buttonMouseListener = new MouseAdapter() {
	    public void mouseEntered(MouseEvent e) {
		updateButtonInfo(id);
	    }
	};

	private ActionListener buttonListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		ObjectKey improvementKey = new
		    ObjectKey(KEY.STATION_IMPROVEMENTS, Player.AUTHORITATIVE,
			    id);
		Move m =
		    AddRemoveStationImprovementMove.generateAddImprovementMove
		    (modelRoot.getWorld(), stationKey, improvementKey);
		modelRoot.getReceiver().processMove(m);
	    }
	};

	public ImprovementJButton(int improvementId) {
	    id = improvementId;
	    setIcon(modelRoot.getViewLists().getImageIcon
		    (new ObjectKey(KEY.STATION_IMPROVEMENTS,
				   Player.AUTHORITATIVE, improvementId),
		     ViewLists.LARGE_ICON));
	    StationImprovement si = (StationImprovement)
		modelRoot.getWorld().get (KEY.STATION_IMPROVEMENTS,
			improvementId, Player.AUTHORITATIVE);
	    setToolTipText(Resources.get(si.getName()));
	    addActionListener(buttonListener);
	    addMouseListener(buttonMouseListener);
	}
    }

    private void updateAvailableImprovements() {
	availableImprovementsJPanel.removeAll();

	StationModel sm = (StationModel) modelRoot.getWorld().get
	    (stationKey.key, stationKey.index, stationKey.principal);
	StationModelViewer smv = new StationModelViewer(modelRoot.getWorld());
	smv.setStationModel(sm);
	for (int i = 0; i <
		modelRoot.getWorld().size(KEY.STATION_IMPROVEMENTS,
		    Player.AUTHORITATIVE); i++) {
	    if (smv.canBuildImprovement(i)) {
		availableImprovementsJPanel.add(new ImprovementJButton(i));
	    }
	}
	availableImprovementsJPanel.repaint();
    }

    /** updates the available improvements button when the set of purchased
     * improvements changes */
    private ListDataListener availableImprovementsListener =
       	new ListDataListener() {
	public void contentsChanged(ListDataEvent e) {
	    if (e.getIndex0() <= stationKey.index &&
		    e.getIndex1() >= stationKey.index) {
		updateAvailableImprovements();
	    }
	}

	public void intervalAdded(ListDataEvent e) {
	    // do nothing
	}

	public void intervalRemoved(ListDataEvent e) {
	    // do nothing
	}
    };

    /**
     * TODO support demolition of improvements by adding a button
     */
    private class PurchasedImprovementsListCellRenderer implements
	ListCellRenderer {
	    private Box cell = new Box(BoxLayout.X_AXIS);
	    private JLabel iconLabel = new JLabel();
	    private JTextArea descriptionLabel = new JTextArea();

	    PurchasedImprovementsListCellRenderer() {
		cell.add(iconLabel);
		cell.add(cell.createHorizontalStrut(4));
		descriptionLabel.setEditable(false);
		descriptionLabel.setLineWrap(true);
		descriptionLabel.setWrapStyleWord(true);
		descriptionLabel.setOpaque(false);
		cell.add(descriptionLabel);
		iconLabel.setHorizontalTextPosition(JLabel.RIGHT);
	    }

	    public Component getListCellRendererComponent(JList list, Object
		    value, int index, boolean isSelected, boolean
		    cellHasFocus) {
		ObjectKey key = (ObjectKey) value;
	    
		StationImprovement si = (StationImprovement)
		    modelRoot.getWorld().get(key.key,
			    key.index, key.principal);

		iconLabel.setIcon(modelRoot.getViewLists().getImageIcon
			(key, ViewLists.LARGE_ICON));
		iconLabel.setText(Resources.get(si.getName()));
		descriptionLabel.setText(Resources.get(si.getDescription()));
		return cell;
	    }
	}

	private ListCellRenderer purchasedImprovementCellRenderer = new
	PurchasedImprovementsListCellRenderer();

    private class PurchasedImprovementsListModel extends AbstractListModel
    implements ListDataListener {

	private int[] improvements;

	public void contentsChanged(ListDataEvent e) {
	    int[] oldImprovements;
	    oldImprovements = improvements;
	    if (e.getIndex0() <= stationKey.index &&
		stationKey.index <= e.getIndex1()) {
		StationModel sm = (StationModel)
		    modelRoot.getWorld().get(stationKey.key, stationKey.index,
		    stationKey.principal);
		improvements = sm.getImprovements();
		if (improvements.length > oldImprovements.length) {
		    fireIntervalAdded(this, oldImprovements.length,
			    improvements.length - 1);
		} else if (improvements.length < oldImprovements.length) {
		    fireIntervalRemoved(this, improvements.length,
			    oldImprovements.length - 1);
		}
		if (Math.min(improvements.length, oldImprovements.length) > 0)
		    fireContentsChanged(this, 0, Math.min(oldImprovements.length,
				improvements.length) - 1);
	    }
	}
	
	public void intervalAdded(ListDataEvent e) {
	    // ignore
	}

	public void intervalRemoved(ListDataEvent e) {
	    // ignore
	}
	
	public PurchasedImprovementsListModel() {
	    w2lma.addListDataListener(this);
	    StationModel stationModel = (StationModel)
		modelRoot.getWorld().get(stationKey.key, stationKey.index,
			stationKey.principal);
		improvements = stationModel.getImprovements();
	}

	public Object getElementAt(int index) {
	    return new ObjectKey(KEY.STATION_IMPROVEMENTS,
		Player.AUTHORITATIVE, improvements[index]);
	}

	public int getSize() {
	    return improvements.length;
	}
    }

    public void setup(ModelRoot mr, ObjectKey stationKey) {
	modelRoot = mr;
	this.stationKey = stationKey;

	w2lma = new World2ListModelAdapter(modelRoot.getWorld(),
		stationKey.key, stationKey.principal,
		modelRoot.getMoveChainFork());

	// setup the list showing already purchased improvements
	currentImprovementsJList.setModel(new
		PurchasedImprovementsListModel());
	currentImprovementsJList.setCellRenderer
	    (purchasedImprovementCellRenderer);

	// initialise the available improvements panel
	w2lma.addListDataListener(availableImprovementsListener);
	updateAvailableImprovements();

	update();
    }

    private void update() {
	ReadOnlyWorld w = modelRoot.getWorld();
	StationModel sm = (StationModel) w.get(stationKey.key,
		stationKey.index, stationKey.principal);

	// display station name
	nameJLabel.setText(sm.getStationName());

	// clear current cargo panels
	demandJPanel.removeAll();
	supplyJPanel.removeAll();

	// display demanded cargo
	for (int i = 0; i < w.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
		i++) {
	    boolean isDemanded = sm.getDemand().isCargoDemanded(i);
	    if (! isDemanded)
		continue;

	    JLabel jl = new JLabel(modelRoot.getViewLists().getTrainImages().
		    getWagonImage(i, ICON_HEIGHT));
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES, i,
		    Player.AUTHORITATIVE);
	    jl.setToolTipText(ct.getDisplayName());
	    demandJPanel.add(jl);
	}	

	// display supplied cargo
	CargoBundle cargoWaiting = (CargoBundle) w.get(KEY.CARGO_BUNDLES, 
		sm.getCargoBundleNumber(), Player.AUTHORITATIVE);
	for (int i = 0; i < w.size(KEY.CARGO_TYPES, Player.AUTHORITATIVE);
		i++) {
	    if (cargoWaiting.getAmount(i) > 0) {
		JLabel jl = new
		    JLabel(modelRoot.getViewLists().getTrainImages().
			    getWagonImage(i, ICON_HEIGHT));
		CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES, i,
			Player.AUTHORITATIVE);
		jl.setToolTipText(ct.getDisplayName());
		supplyJPanel.add(jl);
	    }
	}
    }

    /** Creates new form StationJPanel */
    public StationJPanel() {
        initComponents();
	jScrollPane2.getViewport().setMinimumSize(new Dimension(200, 150));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameJLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        stationStatusJPanel = new javax.swing.JPanel();
        supplyJPanel = new javax.swing.JPanel();
        demandJPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        currentImprovementsJList = new javax.swing.JList();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availableImprovementsJPanel = new ButtonPanel();
        purchaseInfoJPanel = new javax.swing.JPanel();
        impNameJLabel = new javax.swing.JLabel();
        impCostJLabel = new javax.swing.JLabel();
        impDescrJTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        nameJLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        nameJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        nameJLabel.setText("Foo Station");
        add(nameJLabel, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        stationStatusJPanel.setLayout(new java.awt.GridLayout(0, 1));

        supplyJPanel.setBorder(new javax.swing.border.TitledBorder(org.railz.util.Resources.get("Supply")));
        stationStatusJPanel.add(supplyJPanel);

        demandJPanel.setBorder(new javax.swing.border.TitledBorder(org.railz.util.Resources.get("Demand")));
        stationStatusJPanel.add(demandJPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(stationStatusJPanel, gridBagConstraints);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.X_AXIS));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 150));
        currentImprovementsJList.setFixedCellWidth(300);
        currentImprovementsJList.setVisibleRowCount(3);
        jScrollPane1.setViewportView(currentImprovementsJList);

        jPanel2.add(jScrollPane1);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel2.add(jSeparator1);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        availableImprovementsJPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPane2.setViewportView(availableImprovementsJPanel);

        jPanel3.add(jScrollPane2);

        purchaseInfoJPanel.setLayout(new java.awt.GridBagLayout());

        purchaseInfoJPanel.setMinimumSize(new java.awt.Dimension(200, 100));
        impNameJLabel.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        purchaseInfoJPanel.add(impNameJLabel, gridBagConstraints);

        impCostJLabel.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        purchaseInfoJPanel.add(impCostJLabel, gridBagConstraints);

        impDescrJTextArea.setEditable(false);
        impDescrJTextArea.setLineWrap(true);
        impDescrJTextArea.setRows(4);
        impDescrJTextArea.setWrapStyleWord(true);
        impDescrJTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        purchaseInfoJPanel.add(impDescrJTextArea, gridBagConstraints);

        jPanel3.add(purchaseInfoJPanel);

        jPanel2.add(jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 4, 4);
        jPanel1.add(jPanel2, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel availableImprovementsJPanel;
    private javax.swing.JList currentImprovementsJList;
    private javax.swing.JPanel demandJPanel;
    private javax.swing.JLabel impCostJLabel;
    private javax.swing.JTextArea impDescrJTextArea;
    private javax.swing.JLabel impNameJLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel nameJLabel;
    private javax.swing.JPanel purchaseInfoJPanel;
    private javax.swing.JPanel stationStatusJPanel;
    private javax.swing.JPanel supplyJPanel;
    // End of variables declaration//GEN-END:variables
    
    private class ButtonPanel extends JPanel implements Scrollable {
	public Dimension getPreferredScrollableViewportSize() {
	    Dimension d = getPreferredSize();
	    return d;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int
		orientation, int direction) {
	    switch (direction) {
		case SwingConstants.VERTICAL:
		    return getHeight() / 2;
		default:
		    return 0;
	    }
	}

	public boolean getScrollableTracksViewportHeight() {
	    return false;
	}

	public boolean getScrollableTracksViewportWidth() {
	    return true;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int
		orientation, int direction) {
	    return 10;
	}

	public Dimension getPreferredSize() {
	    Dimension pSize = super.getPreferredSize();
	    Dimension mSize = getMinimumSize();
	    if (pSize.width < mSize.width)
		pSize.width = mSize.width;
	    if (pSize.height < mSize.height)
		pSize.height = mSize.height;
	    return pSize;
	}
    }
}
