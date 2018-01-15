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
 * Launcher.java
 *
 */

package freerails.client.launcher;

import freerails.client.ClientConfig;
import freerails.client.GameLoop;
import freerails.client.ScreenHandler;
import freerails.controller.ServerControlInterface;
import freerails.network.FreerailsGameServer;
import freerails.network.InetConnectionAccepter;
import freerails.network.LogOnResponse;
import freerails.network.SaveGamesManager;
import freerails.server.SaveGameManagerImpl;
import freerails.server.ServerGameModel;
import freerails.server.ServerGameModelImpl;
import freerails.world.game.GameModel;
import org.apache.log4j.*;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Launcher GUI for both the server and/or client.
 *
 * TODO The code in the switch statements needs reviewing.
 */

public class Launcher extends javax.swing.JFrame implements LauncherInterface {

    /**
     * server sleeping time in ms (1000/SERVERUPDATE is the frame rate)
     */
    private static final int SERVERUPDATE = 50;


    private static final Logger logger = Logger.getLogger(Launcher.class.getName());
    private static final long serialVersionUID = -8224003315973977661L;

    private final Component[] wizardPages = new Component[4];
    private final Icon errorIcon = new javax.swing.ImageIcon(getClass().getResource(ClientConfig.ICON_ERROR));
    private final Icon warningIcon = new javax.swing.ImageIcon(getClass().getResource(ClientConfig.ICON_WARNING));
    private final Icon infoIcon = new javax.swing.ImageIcon(getClass().getResource(ClientConfig.ICON_INFO));
    private final ProgressPanelModel progressPanel = new ProgressPanelModel(this);
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private int currentPage = 0;
    private FreerailsGameServer server;
    private GUIClient client;
    private Properties props;
    private boolean nextIsStart = false;

    private Launcher(boolean quickstart) {
        loadProps();
        initComponents();

        wizardPages[0] = new LauncherPanel1();
        wizardPages[1] = new SelectMapPanel(this);
        wizardPages[2] = new ClientOptionsPanel(this);
        wizardPages[3] = new ConnectedPlayersPanel();

        if (!quickstart) {
            jPanel1.add(wizardPages[0], "0");
            jPanel1.add(wizardPages[1], "1");
            jPanel1.add(wizardPages[2], "2");
            jPanel1.add(wizardPages[3], "3");
            jPanel1.add(progressPanel, "4");
            pack();
        } else {
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            pack();
        }
        hideAllMessages();
    }

    /**
     * Starts the client and server in the same thread.
     */
    private static void startThread(final GameModel server, final GUIClient client) {
        startThread(server);
        try {
            Runnable run = () -> {
                while (null == client.getWorld()) {
                    client.update();
                    server.update();
                }

                GameModel[] models = new GameModel[]{client};
                ScreenHandler screenHandler = client.getScreenHandler();
                Runnable gameLoop = new GameLoop(screenHandler, models);
                // screenHandler.apply();
                gameLoop.run();
            };

            Thread t = new Thread(run, "Client main loop");
            t.start();
        } catch (Exception e) {
            emergencyStop();
        }
    }

    /**
     * Starts the server in a new thread.
     */
    private static void startThread(final GameModel server) {
        try {
            Runnable r = () -> {

                // TODO do this without while(true)
                while (true) {
                    long startTime = System.currentTimeMillis();
                    server.update();
                    long deltatime = System.currentTimeMillis() - startTime;
                    if (deltatime < SERVERUPDATE) {
                        try {
                            Thread.sleep(SERVERUPDATE - deltatime);
                        } catch (InterruptedException e) {
                            // do nothing.
                        }
                    }
                }

            };

            Thread t = new Thread(r, "FreerailsGameServer");
            t.start();
        } catch (Exception e) {
            emergencyStop();
        }
    }

    /**
     * Runs the game.
     */
    public static void main(String args[]) {

        // SynchronizedEventQueue.use();

        // Let the user know if we are using a custom logging config.

        try {
            PatternLayout patternLayout = new PatternLayout("%r [%t] %-5p %m -- at %l%n");
            Appender consoleAppender = new ConsoleAppender(patternLayout);
            Logger rootLogger = LogManager.getRootLogger();
            rootLogger.addAppender(consoleAppender);
            rootLogger.setLevel(Level.INFO);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Started launcher.");
        }
        boolean quickstart = false;
        if (args.length > 0) {
            for (String arg : args) {
                String QUICKSTART = "-quickstart";
                if (QUICKSTART.equals(arg)) quickstart = true;
            }

        }
        Launcher launcher = new Launcher(quickstart);
        launcher.start(quickstart);
    }

    /**
     *
     */
    public static void emergencyStop() {
        ScreenHandler.exitFullScreenMode();
        if (!EventQueue.isDispatchThread()) {
            Thread.currentThread().stop();
        }
    }

    /**
     * Starts the client in a new thread.
     */
    private static void startThread(final GUIClient guiClient) {
        try {
            Runnable run = () -> {
                while (null == guiClient.getWorld()) {
                    guiClient.update();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                GameModel[] models = new GameModel[]{guiClient};
                ScreenHandler screenHandler = guiClient.getScreenHandler();
                Runnable gameLoop = new GameLoop(screenHandler, models);
                gameLoop.run();
            };

            Thread t = new Thread(run, "Client main loop");
            t.start();
        } catch (Exception e) {
            emergencyStop();
        }
    }

    /**
     * Exit the Application.
     */
    private static void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }

    public void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        if (nextIsStart) {
            nextButton.setText("Start");
        } else {
            nextButton.setText("Next...");
        }
    }

    private void startGame() {
        CardLayout cl = (CardLayout) jPanel1.getLayout();
        cl.show(jPanel1, "4");

        setButtonsVisible(false);
        LauncherPanel1 lp = (LauncherPanel1) wizardPages[0];
        SelectMapPanel msp = (SelectMapPanel) wizardPages[1];
        ClientOptionsPanel cop = (ClientOptionsPanel) wizardPages[2];
        ConnectedPlayersPanel cp = (ConnectedPlayersPanel) wizardPages[3];

        boolean recover = false;
        int mode;

        switch (lp.getMode()) {
            case LauncherPanel1.MODE_SINGLE_PLAYER:
                try {

                    mode = cop.getScreenMode();

                    client = new GUIClient(cop.getPlayerName(), progressPanel, mode, cop.getDisplayMode());
                    if (isNewGame()) {
                        initServer();
                    }
                    client.connect(server, cop.getPlayerName(), "password");

                    setServerGameModel();
                } catch (Exception e) {
                    setInfoText(e.getMessage(), MSG_TYPE.WARNING);
                    recover = true;
                } finally {
                    if (recover) {
                        cop.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setButtonsVisible(true);
                        currentPage = 1;
                        cl.show(jPanel1, "1");
                        return;
                    }
                }
                startThread(server, client);
                break;
            case LauncherPanel1.MODE_START_NETWORK_GAME:
                // LL: I don't think this code ever executes now that there is a
                // connected players screen.
                try {
                    setServerGameModel();
                    currentPage = 3;
                    String[] playerNames = server.getPlayerNames();
                    playerNames = playerNames.length == 0 ? new String[]{"No players are connected."} : playerNames;
                    cp.setListOfPlayers(playerNames);
                    cl.show(jPanel1, "3");
                    setNextEnabled(false);
                } catch (Exception e) {
                    // We end up here if an Exception was thrown when loading a
                    // saved game.
                    setInfoText(e.getMessage(), MSG_TYPE.WARNING);
                    recover = true;
                } finally {
                    if (recover) {
                        cop.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setNextEnabled(true);
                        currentPage = 1;
                        setButtonsVisible(true);
                        cl.show(jPanel1, "1");
                        return;
                    }
                }

                break;
            case LauncherPanel1.MODE_JOIN_NETWORK_GAME:
                mode = cop.getScreenMode();
                try {

                    InetSocketAddress serverInetAddress = cop.getRemoteServerAddress();
                    if (null == serverInetAddress) {
                        throw new NullPointerException("Couldn't resolve hostname.");
                    }
                    String playerName = cop.getPlayerName();
                    client = new GUIClient(playerName, progressPanel, mode, cop.getDisplayMode());

                    String hostname = serverInetAddress.getHostName();
                    int port = serverInetAddress.getPort();
                    setInfoText("Connecting to server...", MSG_TYPE.INFO);
                    LogOnResponse logOnResponse = client.connect(hostname, port, playerName, "password");
                    if (logOnResponse.isSuccessful()) {
                        setInfoText("Logged on and waiting for game to start.", MSG_TYPE.INFO);
                        startThread(client);
                    } else {
                        recover = true;
                        setInfoText(logOnResponse.getMessage(), MSG_TYPE.WARNING);
                    }
                } catch (NullPointerException e) {
                    setInfoText(e.getMessage(), MSG_TYPE.WARNING);
                    recover = true;
                } finally {
                    if (recover) {
                        cop.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setButtonsVisible(true);
                        cl.show(jPanel1, "2");
                        return;
                    }
                }

                break;
            case LauncherPanel1.MODE_SERVER_ONLY:
                if (msp.validateInput()) {
                    initServer();
                    try {
                        setServerGameModel();

                        prepare2HostNetworkGame(msp.getServerPort());
                        setNextEnabled(true);
                    } catch (NullPointerException | IOException e) {
                        setInfoText(e.getMessage(), MSG_TYPE.WARNING);
                        recover = true;
                    } finally {
                        if (recover) {
                            cop.setControlsEnabled(true);
                            prevButton.setEnabled(true);
                            setButtonsVisible(true);
                        }
                    }

                }
        }// End of switch statement
    }

    private void setServerGameModel() {

        ClientOptionsPanel cop = (ClientOptionsPanel) wizardPages[2];
        if (isNewGame()) {
            SelectMapPanel msp2 = (SelectMapPanel) wizardPages[1];
            server.newGame(msp2.getNewMapName());
            cop.limitPlayerNames(null);
        }  // Do nothing since the server is already set up.

    }

    private boolean isNewGame() {
        SelectMapPanel msp2 = (SelectMapPanel) wizardPages[1];

        return msp2.getSelection() == MapSelection.NEW_GAME;
    }

    private void initServer() {
        SaveGamesManager gamesManager = new SaveGameManagerImpl();
        server = new FreerailsGameServer(gamesManager);
        ServerGameModel serverGameModel = new ServerGameModelImpl();
        server.setServerGameModel(serverGameModel);

        /*
         * Set the server field on the connected players panel so that it can
         * keep track of who is connected.
         */
        ConnectedPlayersPanel cp = (ConnectedPlayersPanel) wizardPages[3];
        cp.server = server;
        server.addPropertyChangeListener(cp);
        cp.updateListOfPlayers();
    }

    /**
     * Shows GUI. If {@code quickstart} is {@code true} runs the
     * game.
     *
     * @param quickstart boolean
     */
    private void start(boolean quickstart) {
        setVisible(true);
        if (quickstart) {
            startGame();
        }
    }

    /**
     * Starts a thread listening for new connections.
     */
    private void prepare2HostNetworkGame(int port) throws IOException {
        loadProps();
        if (isNewGame()) {
            initServer();
        }
        InetConnectionAccepter accepter = new InetConnectionAccepter(port, server);
        /*
         * Note, the thread's name gets set in the run method so there is no
         * point setting it here.
         */
        Thread t = new Thread(accepter);
        t.start();

        CardLayout cl = (CardLayout) jPanel1.getLayout();
        cl.show(jPanel1, "3");
        currentPage = 3;
    }


    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Freerails Launcher");
        addWindowListener(new MyWindowAdapter());

        jPanel1.setLayout(new java.awt.CardLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(400, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        nextButton.setText("Next...");
        nextButton.addActionListener(this::nextButtonActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(nextButton, gridBagConstraints);

        prevButton.setText("Back...");
        prevButton.setEnabled(false);
        prevButton.addActionListener(this::prevButtonActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(prevButton, gridBagConstraints);

        infoLabel.setText("Error messages go here!");
        infoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        infoLabel.setMinimumSize(new java.awt.Dimension(20, 20));
        infoLabel.setPreferredSize(new java.awt.Dimension(20, 20));
        infoLabel.setVerifyInputWhenFocusTarget(false);
        infoLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(infoLabel, gridBagConstraints);

        pack();
    }

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {
        CardLayout cl = (CardLayout) jPanel1.getLayout();
        nextIsStart = false;
        hideAllMessages();
        switch (currentPage) {
            case 1:
                cl.previous(jPanel1);
                currentPage--;
                prevButton.setEnabled(false);
                break;
            case 2:
                LauncherPanel1 panel = (LauncherPanel1) wizardPages[0];
                if (panel.getMode() == LauncherPanel1.MODE_JOIN_NETWORK_GAME) {
                    currentPage = 0;
                    cl.show(jPanel1, "0");
                    prevButton.setEnabled(false);
                } else {
                    currentPage--;
                    cl.previous(jPanel1);
                }
        }
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            CardLayout cl = (CardLayout) jPanel1.getLayout();
            LauncherPanel1 panel = (LauncherPanel1) wizardPages[0];
            SelectMapPanel msp = (SelectMapPanel) wizardPages[1];
            ClientOptionsPanel cop = (ClientOptionsPanel) wizardPages[2];
            hideAllMessages();

            switch (currentPage) {
                case 0:
                    msp.validateInput();
                    /* Initial game selection page */
                    switch (panel.getMode()) {
                        case LauncherPanel1.MODE_SERVER_ONLY:
                            /* go to map selection screen */
                            cl.next(jPanel1);
                            msp.setServerPortPanelVisible(true);

                            currentPage++;
                            break;
                        case LauncherPanel1.MODE_SINGLE_PLAYER:
                            /* go to map selection screen */
                            cl.next(jPanel1);
                            msp.setServerPortPanelVisible(false);
                            cop.setRemoteServerPanelVisible(false);
                            currentPage++;
                            break;
                        case LauncherPanel1.MODE_START_NETWORK_GAME:
                            /* go to map selection screen */
                            msp.setServerPortPanelVisible(true);
                            cop.setRemoteServerPanelVisible(false);
                            cl.next(jPanel1);
                            currentPage++;
                            break;
                        case LauncherPanel1.MODE_JOIN_NETWORK_GAME:
                            /* client display options */
                            nextIsStart = true;
                            cl.show(jPanel1, "2");
                            currentPage = 2;
                            msp.setServerPortPanelVisible(false);
                            cop.setRemoteServerPanelVisible(true);
                            cop.limitPlayerNames(null);
                            break;
                    }
                    prevButton.setEnabled(true);
                    break;
                case 1:
                    /* map selection page */
                    if (panel.getMode() == LauncherPanel1.MODE_SERVER_ONLY) {
                        if (msp.validateInput()) {
                            prevButton.setEnabled(false);
                            try {
                                if (!isNewGame()) {
                                    initServer();
                                    server.loadgame(ServerControlInterface.FREERAILS_SAV);
                                }
                                prepare2HostNetworkGame(msp.getServerPort());
                            } catch (BindException be) {
                                // When the port is already in use.
                                prevButton.setEnabled(true);
                                setInfoText(be.getMessage(), MSG_TYPE.WARNING);
                            }
                        }
                    } else {
                        if (isNewGame()) {
                            cop.limitPlayerNames(null);
                        } else {
                            initServer();
                            server.loadgame(msp.getSaveGameName());
                            String[] playerNames = server.getPlayerNames();
                            cop.limitPlayerNames(playerNames);
                        }

                        nextIsStart = true;
                        prevButton.setEnabled(true);
                        setNextEnabled(true);
                        currentPage++;
                        cl.next(jPanel1);
                    }

                    break;
                case 2:
                    /* display mode selection */
                    if (panel.getMode() == LauncherPanel1.MODE_START_NETWORK_GAME) {
                        if (msp.validateInput()) {
                            prevButton.setEnabled(false);
                            int mode = cop.getScreenMode();

                            prepare2HostNetworkGame(msp.getServerPort());
                            client = new GUIClient(cop.getPlayerName(), progressPanel, mode, cop.getDisplayMode());
                            client.connect(server, cop.getPlayerName(), "password");
                        }
                    } else {

                        prevButton.setEnabled(false);
                        cop.setControlsEnabled(false);
                        startGame();
                    }
                    break;
                case 3:
                    try {
                        /* Connection status screen */
                        prevButton.setEnabled(false);
                        setServerGameModel();// TODO catch exception
                        if (panel.getMode() == LauncherPanel1.MODE_START_NETWORK_GAME) {
                            startThread(server, client);
                            cl.show(jPanel1, "4");
                        } else {
                            /* Start a stand alone server. */
                            startThread(server);
                            setVisible(false);
                        }
                        setButtonsVisible(false);
                        setNextEnabled(false);
                    } catch (Exception e) {
                        setInfoText(e.getMessage(), MSG_TYPE.WARNING);
                        cop.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setNextEnabled(true);
                        currentPage = 1;
                        cl.show(jPanel1, "1");
                        return;
                    }
                    break;
                default:
                    throw new IllegalArgumentException(String.valueOf(currentPage));
            }
        } catch (Exception e) {
            emergencyStop();
        }
    }


    /**
     * @param text
     * @param status
     */

    public void setInfoText(String text, MSG_TYPE status) {
        infoLabel.setText(text);
        switch (status) {
            case ERROR:
                infoLabel.setIcon(errorIcon);
                nextButton.setEnabled(false);
                break;
            case INFO:
                infoLabel.setIcon(infoIcon);
                nextButton.setEnabled(true);
                break;
            case WARNING:
                infoLabel.setIcon(warningIcon);
                nextButton.setEnabled(true);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(status));
        }

    }

    /**
     *
     */
    public void hideAllMessages() {
        infoLabel.setText(null);
        infoLabel.setIcon(null);
        nextButton.setEnabled(true);

    }

    /**
     * @param b
     */
    private void setButtonsVisible(boolean b) {
        nextButton.setVisible(b);
        prevButton.setVisible(b);
    }

    /**
     *
     */
    public void hideErrorMessages() {
        if (infoLabel.getIcon() == errorIcon) {
            infoLabel.setText(null);
            infoLabel.setIcon(null);
            nextButton.setEnabled(true);
        }
    }

    private void loadProps() {
        try {
            props = new Properties();
            FileInputStream in = new FileInputStream(PROPERTIES_FILENAME);
            props.load(in);
            in.close();
            if (!props.containsKey(SERVER_PORT_PROPERTY) || !props.containsKey(PLAYER_NAME_PROPERTY) || !props.containsKey(SERVER_IP_ADDRESS_PROPERTY)) {
                throw new Exception();
            }
        } catch (Exception e) {
            props = new Properties();
            props.setProperty(SERVER_PORT_PROPERTY, "55000");
            props.setProperty(PLAYER_NAME_PROPERTY, System.getProperty("user.name"));
            props.setProperty(SERVER_IP_ADDRESS_PROPERTY, "127.0.0.1");
        }
    }

    /**
     *
     */
    public void saveProps() {
        try {
            FileOutputStream out = new FileOutputStream(PROPERTIES_FILENAME);
            props.store(out, "---No Comment---");
            out.close();

            // Copy key-value pairs to System.Properties so
            // that they are visible in the game via the
            // show java properties menu item.
            System.getProperties().putAll(props);

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    private static class MyWindowAdapter extends java.awt.event.WindowAdapter {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            exitForm(e);
        }
    }
}
