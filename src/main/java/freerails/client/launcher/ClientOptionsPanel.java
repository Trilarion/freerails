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
 * ClientOptionsPanel.java
 *
 */

package freerails.client.launcher;

import freerails.client.ClientConfig;
import freerails.client.view.DisplayModesComboBoxModels;
import freerails.controller.MyDisplayMode;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.InetSocketAddress;

/**
 * The Launcher panel that lets you choose fullscreen or windowed mode and the
 * screen resolution etc.
 */
class ClientOptionsPanel extends JPanel {

    private static final long serialVersionUID = 3256721779883325748L;
    private static final Logger logger = Logger.getLogger(ClientOptionsPanel.class.getName());
    private static final String INVALID_PORT = "A valid port value is between between 0 and 65535.";
    private final LauncherInterface owner;
    private JRadioButton fixedSizeButton;
    private JRadioButton fullScreenButton;
    private JList list1;
    private JPanel jPanel4;
    private JTextField playerName;
    private JComboBox playerNames;
    private JTextField remoteIP;
    private JTextField remotePort;
    private JRadioButton windowedButton;
    private String[] names;

    public ClientOptionsPanel(LauncherInterface owner) {
        this.owner = owner;
        GridBagConstraints gridBagConstraints;

        ButtonGroup buttonGroup1 = new ButtonGroup();
        JPanel jPanel3 = new JPanel();
        JLabel label1 = new JLabel();
        playerName = new JTextField();
        playerNames = new JComboBox();
        jPanel4 = new JPanel();
        JLabel label2 = new JLabel();
        remoteIP = new JTextField();
        JLabel label3 = new JLabel();
        remotePort = new JTextField();
        JPanel spacer = new JPanel();
        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        list1 = new JList();
        JPanel jPanel2 = new JPanel();
        windowedButton = new JRadioButton();
        fixedSizeButton = new JRadioButton();
        fullScreenButton = new JRadioButton();

        setLayout(new GridBagLayout());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                formComponentShown(e);
            }
        });

        jPanel3.setLayout(new FlowLayout(FlowLayout.LEFT));

        jPanel3.setBorder(new TitledBorder(new EtchedBorder(), "Player Details"));
        label1.setText("Player name:");
        jPanel3.add(label1);

        playerName.setColumns(12);
        playerName.setText(owner.getProperty(ClientConfig.PLAYER_NAME_PROPERTY));
        jPanel3.add(playerName);

        playerNames.setToolTipText("Select a player from the saved game.");
        jPanel3.add(playerNames);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new GridBagLayout());

        jPanel4.setBorder(new TitledBorder(new EtchedBorder(), "Remote server address"));
        jPanel4.setEnabled(false);
        label2.setText("IP Address:");
        jPanel4.add(label2, new GridBagConstraints());

        remoteIP.setColumns(15);
        remoteIP.setText(owner.getProperty(ClientConfig.SERVER_IP_ADDRESS_PROPERTY));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel4.add(remoteIP, gridBagConstraints);

        label3.setText("port");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel4.add(label3, gridBagConstraints);

        remotePort.setColumns(5);
        remotePort.setText(owner.getProperty(ClientConfig.SERVER_PORT_PROPERTY));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel4.add(remotePort, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(spacer, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(jPanel4, gridBagConstraints);

        jPanel1.setLayout(new BorderLayout());

        jPanel1.setBorder(new TitledBorder(new EtchedBorder(), "Select Display Mode"));
        jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));

        String displayMode = owner.getProperty(ClientConfig.CLIENT_DISPLAY_PROPERTY);
        String fullscreenProp = owner.getProperty(ClientConfig.CLIENT_FULLSCREEN_PROPERTY);
        boolean fullscreen = false;
        if (displayMode != null && !displayMode.trim().isEmpty()) {
            fullscreen = Boolean.valueOf(fullscreenProp);
        }

        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!fullscreen) {
            list1.setEnabled(false);
        } else {
            list1.setEnabled(true);
        }

        list1.addListSelectionListener(this::list1ValueChanged);

        jScrollPane1.setViewportView(list1);

        jPanel1.add(jScrollPane1, BorderLayout.CENTER);

        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.Y_AXIS));

        buttonGroup1.add(windowedButton);
        if (!fullscreen) {
            windowedButton.setSelected(true);
        }
        windowedButton.setText("Windowed");
        jPanel2.add(windowedButton);

        buttonGroup1.add(fixedSizeButton);
        fixedSizeButton.setText("Windowed (fixed size 640*480)");
        jPanel2.add(fixedSizeButton);

        buttonGroup1.add(fullScreenButton);
        if (fullscreen) {
            fullScreenButton.setSelected(true);
        }
        fullScreenButton.setText("Full screen");
        fullScreenButton.addChangeListener(this::fullScreenButtonStateChanged);

        jPanel2.add(fullScreenButton);

        jPanel1.add(jPanel2, BorderLayout.NORTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        DisplayModesComboBoxModels listModel = new DisplayModesComboBoxModels();
        listModel.removeDisplayModesBelow(640, 480, 16);
        list1.setModel(listModel);
        int pos = 0;
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.getElementAt(i).toString().equals(displayMode)) {
                pos = i;
                break;
            }
        }
        list1.setSelectedIndex(pos);
        list1.ensureIndexIsVisible(list1.getSelectedIndex());
        validateInput();
        // Listen for changes in the server port text box.
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        };
        remotePort.getDocument().addDocumentListener(documentListener);
        remoteIP.getDocument().addDocumentListener(documentListener);
        playerName.getDocument().addDocumentListener(documentListener);
    }

    /**
     * If the user has opted to load a game, we need to limit the list of
     * players to participants in the game we are loading. Otherwise, any player
     * name is OK. Either, pass in a array of names or null if any name is OK.
     */
    void limitPlayerNames(String[] n) {
        names = n;
        if (names == null) {
            playerName.setVisible(true);
            playerNames.setVisible(false);
            playerName.setEditable(true);
        } else {
            if (names.length == 1) {
                playerName.setVisible(true);
                playerNames.setVisible(false);
                playerName.setText(n[0]);
                playerName.setEditable(false);
            } else {
                playerName.setVisible(false);
                playerNames.setVisible(true);
                ComboBoxModel model = new DefaultComboBoxModel(names);
                playerNames.setModel(model);
            }
        }
        revalidate();
    }

    String getPlayerName() {
        if (playerName.isVisible()) {
            return playerName.getText();
        }
        int index = playerNames.getSelectedIndex();
        if (index < 0) return null; // no selection.
        return names[index];
    }

    DisplayMode getDisplayMode() {
        if (fullScreenButton.isSelected()) {
            MyDisplayMode displayMode = ((MyDisplayMode) list1.getSelectedValue());
            logger.debug("The selected display mode is " + displayMode.toString());
            return displayMode.displayMode;
        }
        return null;
    }

    InetSocketAddress getRemoteServerAddress() {
        String portStr = remotePort.getText();
        if (portStr == null) {
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return null;
        }
        InetSocketAddress address;
        try {
            address = new InetSocketAddress(remoteIP.getText(), port);
        } catch (IllegalArgumentException e) {
            return null;
        }
        /*
         * cut and pasted InetSocketAddress isa = getRemoteServerAddress(); if
         * (isa == null) { infoText = "Please enter a valid remote server
         * address"; } else if (isa.isUnresolved()) { infoText = "Couldn't
         * resolve remote server address"; } else { isValid = true; }
         */
        return address;
    }

    private void validateInput() {
        // Validate player name.
        if (playerName.getText() == null || playerName.getText().isEmpty()) {
            owner.setInfoText("Please set a name for your player", InfoMessageType.ERROR);
            return;
        }

        // Validate host name.
        if (remoteIP.getText() == null || remoteIP.getText().isEmpty()) {
            owner.setInfoText("Please enter a host name", InfoMessageType.ERROR);
            return;
        }

        // Validate port.
        try {
            int port = Integer.parseInt(remotePort.getText());
            if (port < 0 || port > 65535) {
                owner.setInfoText(INVALID_PORT, InfoMessageType.ERROR);
                return;
            }
        } catch (Exception e) {
            owner.setInfoText(INVALID_PORT, InfoMessageType.ERROR);
            return;
        }

        /*
         * Validate display-mode selection. Note, on some systems the display
         * mode can't be changed, in which case the list of selectable display
         * modes will have length 0.
         */
        if (fullScreenButton.isSelected() && list1.getModel().getSize() > 0 && list1.getSelectedIndex() == -1) {
            owner.setInfoText("Select a display-mode.", InfoMessageType.ERROR);
            return;
        }

        // Everything is success.
        owner.hideErrorMessages();

        owner.setProperty(ClientConfig.SERVER_PORT_PROPERTY, remotePort.getText());
        owner.setProperty(ClientConfig.PLAYER_NAME_PROPERTY, playerName.getText());
        owner.setProperty(ClientConfig.SERVER_IP_ADDRESS_PROPERTY, remoteIP.getText());
        owner.setProperty(ClientConfig.CLIENT_FULLSCREEN_PROPERTY, Boolean.toString(fullScreenButton.isSelected()));
        if (getDisplayMode() != null) {
            owner.setProperty(ClientConfig.CLIENT_DISPLAY_PROPERTY, new MyDisplayMode(getDisplayMode()).toString());
        }
        owner.saveProperties();
    }

    int getScreenMode() {
        if (fullScreenButton.isSelected()) {
            return ClientConfig.FULL_SCREEN;
        } else if (windowedButton.isSelected()) {
            return ClientConfig.WINDOWED_MODE;
        } else if (fixedSizeButton.isSelected()) {
            return ClientConfig.FIXED_SIZE_WINDOWED_MODE;
        } else {
            throw new IllegalStateException();
        }
    }

    public void setControlsEnabled(boolean enabled) {
        windowedButton.setEnabled(enabled);
        fullScreenButton.setEnabled(enabled);
        fixedSizeButton.setEnabled(enabled);
        if (fullScreenButton.isSelected()) {
            list1.setEnabled(enabled);
        }
    }

    void setRemoteServerPanelVisible(boolean b) {
        jPanel4.setVisible(b);
    }

    private void list1ValueChanged(ListSelectionEvent evt) {
        validateInput();
    }

    private void formComponentShown(ComponentEvent evt) {
        validateInput();
    }

    private void fullScreenButtonStateChanged(ChangeEvent evt) {
        list1.setEnabled(fullScreenButton.isSelected());
        validateInput();
    }
}
