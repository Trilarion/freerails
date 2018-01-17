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
 * NewsPaperPanel.java
 *
 */
package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Displays a newspaper headline.
 */
public class NewsPaperPanel extends JPanel implements View {
    
    private static final long serialVersionUID = 3258410638366946868L;
    private final Image pieceOfNewspaper;
    private ActionListener callBack;

    /**
     *
     */
    public NewsPaperPanel() {
        JLabel headline = new JLabel();
        JPanel jPanel1 = new JPanel();
        JLabel anyKeyToContinueJLabel = new JLabel();
        setLayout(null);
        setPreferredSize(new Dimension(640, 400));
        setMinimumSize(new Dimension(640, 400));
        setMaximumSize(new Dimension(640, 400));
        setOpaque(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                formKeyPressed(e);
            }
        });

        headline.setPreferredSize(new Dimension(620, 110));
        headline.setMinimumSize(new Dimension(620, 110));
        headline.setText("NEWSPAPER HEADLINE");
        headline.setForeground(Color.black);
        headline.setBackground(Color.white);
        headline.setHorizontalAlignment(SwingConstants.CENTER);
        headline.setFont(new Font("Lucida Bright", 1, 36));
        headline.setMaximumSize(new Dimension(620, 110));

        add(headline);
        headline.setBounds(10, 70, 620, 110);

        jPanel1.setBorder(new BevelBorder(0));

        anyKeyToContinueJLabel.setText("Click to continue");
        anyKeyToContinueJLabel.setForeground(Color.black);
        anyKeyToContinueJLabel.setBackground(Color.darkGray);
        jPanel1.add(anyKeyToContinueJLabel);

        add(jPanel1);
        jPanel1.setBounds(230, 260, 190, 30);

        Image tempImage = (new ImageIcon(getClass().getResource("/freerails/data/newspaper.png"))).getImage();

        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        pieceOfNewspaper = defaultConfiguration.createCompatibleImage(tempImage.getWidth(null), tempImage.getHeight(null), Transparency.BITMASK);

        Graphics g = pieceOfNewspaper.getGraphics();

        g.drawImage(tempImage, 0, 0, null);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                callBack.actionPerformed(new ActionEvent(this, 0, null));
            }
        });
    }


    private void formKeyPressed(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
        setVerifyInputWhenFocusTarget(false);
    }


    @Override
    public void paint(Graphics g) {
        g.drawImage(pieceOfNewspaper, 0, 0, null);
        paintChildren(g);
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        callBack = closeAction;
    }

}