/*
 * ProgressJPanel.java
 *
 * Created on 31 August 2005
 */

package jfreerails.launcher;
import jfreerails.util.FreerailsProgressMonitor;

/**
 * A JPanel that displays a splash screen and a progress bar.
 *
 * @author Luke
 */
public class ProgressJPanel extends javax.swing.JPanel implements
        FreerailsProgressMonitor{
    
    private static final long serialVersionUID = 3256445798203273776L;
    
    int step, stepSize;
    
    final int numSteps = 5;
    
    LauncherInterface owner;
    
    public void setValue(int i) {
        int value = i * 100 / stepSize;
        value += 100 * step;
        progressBar.setValue(value);
    }
    
    public void nextStep(int max) {
    	
    	//So that the waiting for game to start message
    	//goes away.    	
    	owner.hideAllMessages(); 
        
    	
    	step++;
        stepSize = max;
        if(numSteps < step)
            throw new IllegalStateException();
    }
    
    public void finished() {
        if(numSteps-1 != step)
            throw new IllegalStateException(numSteps +"!="+ step);
        
        getTopLevelAncestor().setVisible(false);
    }
    
    /** Creates new form ProgressJPanel */
    public ProgressJPanel(LauncherInterface owner) {
    	this.owner = owner;
        initComponents();
        progressBar.setMaximum(numSteps * 100);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        progressBar = new javax.swing.JProgressBar();
        splashImage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        add(progressBar, gridBagConstraints);

        splashImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        splashImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/client/graphics/splash_screen.jpg")));
        add(splashImage, new java.awt.GridBagConstraints());

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JProgressBar progressBar;
    javax.swing.JLabel splashImage;
    // End of variables declaration//GEN-END:variables
    
}
