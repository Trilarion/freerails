/*
 * SelectEngineJPanel.java
 *
 * Created on 25 December 2002, 23:00
 */

package jfreerails.client.view;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.ModelRoot;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.EngineType;

/**
 * This JPanel lets the user select an engine from a list.
 * 
 * @author lindsal8
 * 
 */
public class SelectEngineJPanel extends javax.swing.JPanel implements View {

	private static final long serialVersionUID = 4122537730158179638L;

	public SelectEngineJPanel() {
		initComponents();
		jList1ValueChanged(null); // Disable the ok button if no engine type
		// is selected.
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		okjButton = new javax.swing.JButton();
		canceljButton = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jList1 = new javax.swing.JList();

		setLayout(new java.awt.GridBagLayout());

		setPreferredSize(new java.awt.Dimension(400, 350));
		okjButton.setText("OK");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(10, 10, 9, 10);
		add(okjButton, gridBagConstraints);

		canceljButton.setText("Cancel");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
		add(canceljButton, gridBagConstraints);

		jList1
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jList1
				.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						jList1ValueChanged(evt);
					}
				});

		jScrollPane1.setViewportView(jList1);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(jScrollPane1, gridBagConstraints);

	}// GEN-END:initComponents

	private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) { // GEN-FIRST:event_jList1ValueChanged
		// We need to disable the OK button if no engine type is selected.

		if (-1 == jList1.getSelectedIndex()) {
			okjButton.setEnabled(false);
		} else {
			okjButton.setEnabled(true);
		}
	} // GEN-LAST:event_jList1ValueChanged

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton canceljButton;

	private javax.swing.JList jList1;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JButton okjButton;

	// End of variables declaration//GEN-END:variables

	final private class TrainCellRenderer implements ListCellRenderer {

		final JLabel label;

		final ViewLists viewLists;

		public TrainCellRenderer(ViewLists vl) {

			viewLists = vl;

			label = new JLabel();
		}

		public Component getListCellRendererComponent(JList list, Object value,
		/* value to display */
		int index, /* cell index */
		boolean isSelected, /* is the cell selected */
		boolean cellHasFocus) /* the list and the cell have the focus */{

			EngineType engine = (EngineType) value;
			label.setFont(new java.awt.Font("Dialog", 0, 12));
			String text = "<html><body>" + (isSelected ? "<strong>" : "")
					+ engine.getEngineTypeName() + "<br>"
					+ engine.getMaxSpeed() + " m.p.h. "
					+ engine.getPowerAtDrawbar() + " hp $"
					+ engine.getPrice().toString()
					+ (isSelected ? "</strong>" : "") + "</body></html>";
			label.setText(text);
			Image image = viewLists.getTrainImages()
					.getSideOnEngineImage(index);
			int height = image.getHeight(null);
			int width = image.getWidth(null);
			int scale = height / 50;
			ImageIcon icon = new ImageIcon(image.getScaledInstance(width
					/ scale, height / scale, Image.SCALE_FAST));
			label.setIcon(icon);

			return label;
		}
	}

	public void setup(ModelRoot mr, ViewLists vl,
			ActionListener submitButtonCallBack) {

		jList1.setModel(new World2ListModelAdapter(mr.getWorld(),
				SKEY.ENGINE_TYPES));
		jList1.setCellRenderer(new TrainCellRenderer(vl));
		okjButton.addActionListener(submitButtonCallBack);
	}

	/**
	 * Removes any existing ActionListener listeners from the cancel button,
	 * then adds the specifed one.
	 */
	void setCancelButtonActionListener(ActionListener l) {
		ActionListener[] oldListeners = canceljButton.getActionListeners();
		for (int i = 0; i < oldListeners.length; i++) {
			canceljButton.removeActionListener(oldListeners[i]);
		}
		this.canceljButton.addActionListener(l);
	}

	/**
	 * Returns the number of the currently selected engine type.
	 * 
	 */
	public int getEngineType() {
		return jList1.getSelectedIndex();
	}
}
