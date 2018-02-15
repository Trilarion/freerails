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
 * SelectWagonsPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.WagonCellRenderer;
import freerails.controller.ModelRoot;
import freerails.model.SKEY;
import freerails.model.train.TrainModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets the user add wagons to a train.
 */

public class SelectWagonsPanel extends JPanel implements View {

    private static final long serialVersionUID = 3905239009449095220L;
    private final Image stationView;
    private final List<Integer> wagons = new ArrayList<>();
    private int engineType = 0;
    private RendererRoot rendererRoot;
    private JLabel label1;
    private JButton okButton;
    private JList wagonTypesJList;

    /**
     *
     */
    public SelectWagonsPanel() {
        GridBagConstraints gridBagConstraints;

        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        wagonTypesJList = new JList();
        okButton = new JButton();
        JButton clearButton = new JButton();
        label1 = new JLabel();

        setLayout(new GridBagLayout());

        setBackground(new Color(0, 255, 51));
        setMinimumSize(new Dimension(640, 400));
        setPreferredSize(new Dimension(620, 380));
        jPanel1.setLayout(new GridBagLayout());

        jPanel1.setMinimumSize(new Dimension(170, 300));
        jPanel1.setPreferredSize(new Dimension(170, 300));
        wagonTypesJList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                wagonTypesJListKeyTyped(e);
            }
        });
        wagonTypesJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                wagonTypesJListMouseClicked(e);
            }
        });

        jScrollPane1.setViewportView(wagonTypesJList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        okButton.setText("OK");

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        jPanel1.add(okButton, gridBagConstraints);

        clearButton.setText("Clear");
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this::Button1ActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        jPanel1.add(clearButton, gridBagConstraints);

        label1.setFont(new Font("Dialog", 0, 10));
        label1.setText("The maximum train length is 6 wagons");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(8, 8, 8, 8);
        jPanel1.add(label1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(20, 400, 70, 10);
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        add(jPanel1, gridBagConstraints);

        updateMaxWagonsText();

        URL url = SelectWagonsPanel.class.getResource("/freerails/data/station.gif");
        Image tempImage = (new ImageIcon(url)).getImage();

        GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        stationView = defaultConfiguration.createCompatibleImage(tempImage.getWidth(null), tempImage.getHeight(null), Transparency.BITMASK);

        Graphics g = stationView.getGraphics();

        g.drawImage(tempImage, 0, 0, null);
    }

    /**
     *
     */
    public void resetSelectedWagons() {
        wagons.clear();
    }


    private void wagonTypesJListMouseClicked(MouseEvent evt) {
        // Add your handling code here:
        addwagon();
    }

    private void wagonTypesJListKeyTyped(KeyEvent evt) {
        // Add your handling code here:
        if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
            addwagon();
        }
    }

    // Adds the wagon selected in the list to the train consist.
    private void addwagon() {
        if (wagons.size() < TrainModel.MAX_NUMBER_OF_WAGONS) {
            int type = wagonTypesJList.getSelectedIndex();
            wagons.add(type);

            updateMaxWagonsText();
            repaint();
        }
    }

    private void updateMaxWagonsText() {
        if (wagons.size() >= TrainModel.MAX_NUMBER_OF_WAGONS) {
            label1.setText("Max train length is " + TrainModel.MAX_NUMBER_OF_WAGONS + " wagons");
        } else {
            label1.setText("");
        }
    }

    private void Button1ActionPerformed(ActionEvent evt) {
        // Add your handling code here:
        wagons.clear();
        label1.setText("");
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        // paint the background
        g.drawImage(stationView, 0, 0, null);

        int x = getWidth();

        int y = 330;

        final int SCALED_IMAGE_HEIGHT = 50;
        // paint the wagons
        for (int i = wagons.size() - 1; i >= 0; i--) { // Count down so we
            // paint the wagon
            // at the end of the
            // train first.

            Integer type = wagons.get(i);
            Image image = rendererRoot.getWagonImages(type).getSideOnImage();
            int scaledWidth = image.getWidth(null) * SCALED_IMAGE_HEIGHT / image.getHeight(null);
            x -= scaledWidth;
            g.drawImage(image, x, y, scaledWidth, SCALED_IMAGE_HEIGHT, null);
        }

        // paint the engine
        if (-1 != engineType) { // If an engine is selected.
            Image image = rendererRoot.getEngineImages(engineType).getSideOnImage();

            int scaledWidth = (image.getWidth(null) * SCALED_IMAGE_HEIGHT) / image.getHeight(null);
            x -= scaledWidth;
            g.drawImage(image, x, y, scaledWidth, SCALED_IMAGE_HEIGHT, null);
        }

        paintChildren(g);
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        WorldToListModelAdapter w2lma = new WorldToListModelAdapter(modelRoot.getWorld(), SKEY.CARGO_TYPES);
        wagonTypesJList.setModel(w2lma);
        this.rendererRoot = rendererRoot;
        ListCellRenderer wagonCellRenderer = new WagonCellRenderer(w2lma, this.rendererRoot);
        wagonTypesJList.setCellRenderer(wagonCellRenderer);
        okButton.addActionListener(closeAction);
    }

    /**
     * @return
     */
    public Integer[] getWagons() {
        Integer[] wagonsArray = new Integer[wagons.size()];
        for (int i = 0; i < wagons.size(); i++) {
            Integer type = wagons.get(i);
            wagonsArray[i] = type;
        }
        return wagonsArray;
    }

    /**
     * @param engineType
     */
    public void setEngineType(int engineType) {
        this.engineType = engineType;
    }


}
