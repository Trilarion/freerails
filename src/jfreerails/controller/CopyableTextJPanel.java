/*
 * CopyableTextJPanel.java
 *
 * Created on 09 September 2005, 17:58
 */

package jfreerails.controller;

import javax.swing.SwingUtilities;

/**
 * Displays text that can be selected with the mouse and copied to the clipboard.
 *
 * @author  Luke
 */
public class CopyableTextJPanel extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 4076159955353400345L;

    /** Creates new form CopyableTextJPanel */
    public CopyableTextJPanel() {
        initComponents();
    }
    
    public void setText(String s){
        this.jTextArea1.setText(s);
    }          
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu1 = new javax.swing.JPopupMenu();
        copyItem = new javax.swing.JMenuItem();
        selectAllItem = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyItem.setText("Copy");
        copyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyItemActionPerformed(evt);
            }
        });

        jPopupMenu1.add(copyItem);

        selectAllItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllItem.setText("Select All");
        selectAllItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllItemActionPerformed(evt);
            }
        });

        jPopupMenu1.add(selectAllItem);

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 300));
        jTextArea1.setEditable(false);
        jTextArea1.setText("dsfasd\n\nsad\nf\nasd\nfa\nsdf\nas\ndf\nas\ndf\nads\nf\nasd\nf\nads\nf\ndsa\nf\ndsa\nf\ndasf\na\ndsf\nads\nf\nasd\nf\nasd\nf\n\nasdf");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);
        jTextArea1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextArea1MouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void selectAllItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllItemActionPerformed
        jTextArea1.selectAll();
    }//GEN-LAST:event_selectAllItemActionPerformed

    private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyItemActionPerformed
       jTextArea1.copy();
    }//GEN-LAST:event_copyItemActionPerformed

    private void jTextArea1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextArea1MouseClicked
        if(SwingUtilities.isRightMouseButton(evt)){
           jPopupMenu1.show(jTextArea1, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jTextArea1MouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JMenuItem copyItem;
    javax.swing.JPopupMenu jPopupMenu1;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JTextArea jTextArea1;
    javax.swing.JMenuItem selectAllItem;
    // End of variables declaration//GEN-END:variables
    
}