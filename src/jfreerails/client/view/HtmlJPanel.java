/*
 * GameControlsJPanel.java
 *
 * Created on 19 April 2003, 16:41
 */

package jfreerails.client.view;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;

/**
 *  This JPanel displays a HTML document read from a URL.
 * @author  Luke
 */
public class HtmlJPanel extends javax.swing.JPanel implements View {
    
    private final URL htmlUrl;
    
    /** Creates new form GameControlsJPanel */
    public HtmlJPanel(URL url) {
        this.htmlUrl = url;
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        htmlJLabel = new javax.swing.JLabel();
        loadText();
        done = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        htmlJLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        htmlJLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jScrollPane1.setViewportView(htmlJLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        done.setText("Close");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(done, gridBagConstraints);

    }//GEN-END:initComponents
    
    public void setup(ReadOnlyWorld w, ViewLists vl, ActionListener submitButtonCallBack) {
        this.done.addActionListener(submitButtonCallBack);
    }
    
    /** Load the help text from file.  */
    public void loadText() {
        try {            
            InputStream in = htmlUrl.openStream();
            BufferedReader br =
            new BufferedReader(
            new InputStreamReader(new DataInputStream(in)));
            String line;
            String text = "";
            while ((line = br.readLine()) != null) {
                text = text + line;
            }
            this.htmlJLabel.setText(text);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(htmlUrl);
            this.htmlJLabel.setText("Couldn't read: "+htmlUrl);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton done;
    private javax.swing.JLabel htmlJLabel;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
}