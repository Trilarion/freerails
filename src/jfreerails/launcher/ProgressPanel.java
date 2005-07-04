/*
 * ProgressPanel.java
 *
 * Created on 17 October 2004, 17:28
 */

package jfreerails.launcher;

import jfreerails.util.FreerailsProgressMonitor;

/**
 * A JPanel that displays a splash screen and a progress bar.
 * 
 * @author Luke
 */
public class ProgressPanel extends javax.swing.JPanel implements
		FreerailsProgressMonitor {

	private static final long serialVersionUID = 3256445798203273776L;

	/** Creates new form ProgressPanel */
	public ProgressPanel() {
		initComponents();
	}

	public void setValue(int i) {
		jProgressBar1.setValue(i);

	}

	public void setMax(int max) {
		jProgressBar1.setMaximum(max);

	}

	public void setMessage(String text) {
		progressJLabel.setText(text);

	}

	public void finished() {
		getTopLevelAncestor().setVisible(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		splashImage = new javax.swing.JLabel();
		progressJLabel = new javax.swing.JLabel();
		jProgressBar1 = new javax.swing.JProgressBar();

		setLayout(new java.awt.GridBagLayout());

		splashImage.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/jfreerails/client/graphics/splash_screen.jpg")));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		add(splashImage, gridBagConstraints);

		progressJLabel.setText("Waiting...");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		add(progressJLabel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1.0;
		add(jProgressBar1, gridBagConstraints);

	}// GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	javax.swing.JProgressBar jProgressBar1;

	javax.swing.JLabel progressJLabel;

	javax.swing.JLabel splashImage;
	// End of variables declaration//GEN-END:variables

}
