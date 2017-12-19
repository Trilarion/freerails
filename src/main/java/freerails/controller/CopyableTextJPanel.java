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
 * CopyableTextJPanel.java
 *
 * Created on 09 September 2005, 17:58
 */

package freerails.controller;

import javax.swing.*;

/**
 * Displays text that can be selected with the mouse and copied to the
 * clipboard.
 */
public class CopyableTextJPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 4076159955353400345L;
    // Variables declaration - do not modify                     
    javax.swing.JMenuItem copyItem;
    javax.swing.JPopupMenu jPopupMenu1;
    javax.swing.JScrollPane jScrollPane1;

    // </editor-fold>                        
    javax.swing.JTextArea jTextArea1;
    javax.swing.JMenuItem selectAllItem;

    /**
     * Creates new form CopyableTextJPanel
     */
    public CopyableTextJPanel() {
        initComponents();
    }

    /**
     * @param s
     */
    public void setText(String s) {
        this.jTextArea1.setText(s);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu1 = new javax.swing.JPopupMenu();
        copyItem = new javax.swing.JMenuItem();
        selectAllItem = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C,
                java.awt.event.InputEvent.CTRL_MASK));
        copyItem.setText("Copy");
        copyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyItemActionPerformed(evt);
            }
        });

        jPopupMenu1.add(copyItem);

        selectAllItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_A,
                java.awt.event.InputEvent.CTRL_MASK));
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
        jTextArea1
                .setText("dsfasd\n\nsad\nf\nasd\nfa\nsdf\nas\ndf\nas\ndf\nads\nf\nasd\nf\nads\nf\ndsa\nf\ndsa\nf\ndasf\na\ndsf\nads\nf\nasd\nf\nasd\nf\n\nasdf");
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

    private void selectAllItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectAllItemActionPerformed
        jTextArea1.selectAll();
    }// GEN-LAST:event_selectAllItemActionPerformed

    private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_copyItemActionPerformed
        jTextArea1.copy();
    }// GEN-LAST:event_copyItemActionPerformed

    private void jTextArea1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jTextArea1MouseClicked
        if (SwingUtilities.isRightMouseButton(evt)) {
            jPopupMenu1.show(jTextArea1, evt.getX(), evt.getY());
        }
    }// GEN-LAST:event_jTextArea1MouseClicked
    // End of variables declaration                   

}
