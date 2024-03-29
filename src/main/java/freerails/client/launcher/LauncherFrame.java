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

import freerails.Options;
import freerails.client.ClientConstants;
import freerails.server.FreerailsGameServer;
import freerails.network.LogOnResponse;
import freerails.scenario.FullSaveGameManager;
import freerails.server.FullServerGameModel;
import freerails.server.GameModel;
import freerails.util.network.Connection;
import freerails.util.network.ServerSocketAcceptor;
import org.apache.log4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO The code in the switch statements needs reviewing.
/**
 * Launcher GUI for both the server and/or client.
 */
public class LauncherFrame extends JFrame implements LauncherInterface {

    private static final Logger logger = Logger.getLogger(LauncherFrame.class.getName());
    private static final long serialVersionUID = -8224003315973977661L;
    private final Component[] wizardPages = new Component[4];
    private final Icon errorIcon = new ImageIcon(getClass().getResource(ClientConstants.ICON_ERROR));
    private final Icon warningIcon = new ImageIcon(getClass().getResource(ClientConstants.ICON_WARNING));
    private final Icon infoIcon = new ImageIcon(getClass().getResource(ClientConstants.ICON_INFO));
    private JLabel infoLabel;
    private JPanel jPanel1;
    private JButton nextButton;
    private JButton prevButton;
    private int currentPage = 0;
    private FreerailsGameServer server;
    private LauncherClient client;
    private boolean nextIsStart = false;

    private LauncherFrame() {
        GridBagConstraints gridBagConstraints;

        jPanel1 = new JPanel();
        nextButton = new JButton();
        prevButton = new JButton();
        infoLabel = new JLabel();

        getContentPane().setLayout(new GridBagLayout());

        setTitle("Freerails Launcher");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        jPanel1.setLayout(new CardLayout());

        jPanel1.setPreferredSize(new Dimension(400, 300));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        nextButton.setText("Next...");
        nextButton.addActionListener(this::nextButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        getContentPane().add(nextButton, gridBagConstraints);

        prevButton.setText("Back...");
        prevButton.setEnabled(false);
        prevButton.addActionListener(this::prevButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        getContentPane().add(prevButton, gridBagConstraints);

        infoLabel.setText("Error messages go here!");
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        infoLabel.setMinimumSize(new Dimension(20, 20));
        infoLabel.setPreferredSize(new Dimension(20, 20));
        infoLabel.setVerifyInputWhenFocusTarget(false);
        infoLabel.setVerticalTextPosition(SwingConstants.TOP);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        getContentPane().add(infoLabel, gridBagConstraints);

        pack();

        wizardPages[0] = new LauncherPanel();
        wizardPages[1] = new SelectMapPanel(this);
        wizardPages[2] = new ClientOptionsPanel(this);
        wizardPages[3] = new ConnectedPlayersPanel();

        jPanel1.add(wizardPages[0], "0");
        jPanel1.add(wizardPages[1], "1");
        jPanel1.add(wizardPages[2], "2");
        jPanel1.add(wizardPages[3], "3");
        pack();
        hideAllMessages();
    }

    /**
     * Starts the client and server in the same thread.
     */
    private static void startThread(final GameModel server, final LauncherClient client) {
        startThread(server);
        try {
            Runnable run = () -> {
                while (null == client.getWorld()) {
                    client.update();
                    server.update();
                }

                ScreenHandler screenHandler = client.getScreenHandler();
                Runnable gameLoop = new GameLoop(screenHandler, client);
                // screenHandler.apply();
                gameLoop.run();
            };

            Thread t = new Thread(run, "Client main loop");
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
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
                    if (deltatime < ClientConstants.SERVERUPDATE) {
                        try {
                            Thread.sleep(ClientConstants.SERVERUPDATE - deltatime);
                        } catch (InterruptedException e) {}
                    }
                }
            };

            Thread t = new Thread(r, "FreerailsGameServer");
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            emergencyStop();
        }
    }

    /**
     * Starts the game.
     */
    public static void main(String[] args) {

        // TODO Let the user know if we are using a custom logging config.

        // Configure logger and short message
        PatternLayout patternLayout = new PatternLayout("%r [%t] %-5p %m -- at %l%n");
        Appender consoleAppender = new ConsoleAppender(patternLayout);
        Logger rootLogger = LogManager.getRootLogger();
        rootLogger.addAppender(consoleAppender);
        rootLogger.setLevel(Level.INFO);
        logger.debug("Started launcher.");

        // load options
        ClientConstants.USER_HOME_FOLDER.mkdirs();
        if (ClientConstants.OPTIONS_FILE.exists()) {
            Options.load(ClientConstants.OPTIONS_FILE);
        }

        // show new launcher frame
        LauncherFrame launcherFrame = new LauncherFrame();
        launcherFrame.setVisible(true);

        // save options
        Options.save(ClientConstants.OPTIONS_FILE);
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
    private static void startThread(final LauncherClient launcherClient) {
        try {
            Runnable run = () -> {
                while (null == launcherClient.getWorld()) {
                    launcherClient.update();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                    }
                }
                ScreenHandler screenHandler = launcherClient.getScreenHandler();
                Runnable gameLoop = new GameLoop(screenHandler, launcherClient);
                gameLoop.run();
            };

            Thread t = new Thread(run, "Client main loop");
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            emergencyStop();
        }
    }

    @Override
    public void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        if (nextIsStart) {
            nextButton.setText("Start");
        } else {
            nextButton.setText("Next...");
        }
    }

    /**
     * Single player local games start here.
     */
    private void startGame() throws IOException {
        CardLayout cl = (CardLayout) jPanel1.getLayout();
        cl.show(jPanel1, "4");

        setButtonsVisible(false);
        LauncherPanel launcherPanel = (LauncherPanel) wizardPages[0];
        SelectMapPanel selectMapPanel = (SelectMapPanel) wizardPages[1];
        ClientOptionsPanel clientOptionsPanel = (ClientOptionsPanel) wizardPages[2];
        ConnectedPlayersPanel connectedPlayersPanel = (ConnectedPlayersPanel) wizardPages[3];

        boolean recover = false;
        int mode;

        switch (launcherPanel.getMode()) {
            case ClientConstants.MODE_START_NETWORK_GAME:
                // LL: I don't think this code ever executes now that there is a connected players screen.
                try {
                    setServerGameModel();
                    currentPage = 3;
                    String[] playerNames = server.getPlayerNames();
                    playerNames = playerNames.length == 0 ? new String[]{"No players are connected."} : playerNames;
                    connectedPlayersPanel.setListOfPlayers(playerNames);
                    cl.show(jPanel1, "3");
                    setNextEnabled(false);
                } catch (Exception e) {
                    // We end up here if an Exception was thrown when loading a saved game.
                    setInfoText(e.getMessage(), InfoMessageType.WARNING);
                    recover = true;
                } finally {
                    if (recover) {
                        clientOptionsPanel.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setNextEnabled(true);
                        currentPage = 1;
                        setButtonsVisible(true);
                        cl.show(jPanel1, "1");
                        return;
                    }
                }

                break;
            case ClientConstants.MODE_JOIN_NETWORK_GAME:
                mode = clientOptionsPanel.getScreenMode();
                try {

                    InetSocketAddress serverInetAddress = clientOptionsPanel.getRemoteServerAddress();
                    if (null == serverInetAddress) {
                        throw new NullPointerException("Couldn't resolve hostname.");
                    }
                    String playerName = clientOptionsPanel.getPlayerName();
                    client = new LauncherClient(playerName, mode, clientOptionsPanel.getDisplayMode());

                    String hostname = serverInetAddress.getHostName();
                    int port = serverInetAddress.getPort();
                    setInfoText("Connecting to server...", InfoMessageType.INFO);
                    LogOnResponse logOnResponse = client.connect(hostname, port, playerName, "password");
                    if (logOnResponse.isSuccess()) {
                        setInfoText("Logged on and waiting for game to start.", InfoMessageType.INFO);
                        startThread(client);
                    } else {
                        recover = true;
                        setInfoText(logOnResponse.getMessage(), InfoMessageType.WARNING);
                    }
                } catch (NullPointerException e) {
                    setInfoText(e.getMessage(), InfoMessageType.WARNING);
                    recover = true;
                } finally {
                    if (recover) {
                        clientOptionsPanel.setControlsEnabled(true);
                        prevButton.setEnabled(true);
                        setButtonsVisible(true);
                        cl.show(jPanel1, "2");
                        return;
                    }
                }

                break;
            case ClientConstants.MODE_SERVER_ONLY:
                if (selectMapPanel.validateInput()) {
                    initServer();
                    try {
                        setServerGameModel();

                        prepareToHostNetworkGame(selectMapPanel.getServerPort());
                        setNextEnabled(true);
                    } catch (NullPointerException e) {
                        setInfoText(e.getMessage(), InfoMessageType.WARNING);
                        recover = true;
                    } finally {
                        if (recover) {
                            clientOptionsPanel.setControlsEnabled(true);
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
        }   // Do nothing since the server is already set up.
    }

    private boolean isNewGame() {
        SelectMapPanel msp2 = (SelectMapPanel) wizardPages[1];
        return msp2.getSelection() == MapSelection.NEW_GAME;
    }

    private void initServer() {

        server = new FreerailsGameServer(new FullSaveGameManager());
        server.setServerGameModel(new FullServerGameModel());
        /*
         * Set the server field on the connected players panel so that it can
         * keep track of who is connected.
         */
        ConnectedPlayersPanel connectedPlayersPanel = (ConnectedPlayersPanel) wizardPages[3];
        connectedPlayersPanel.server = server;
        server.addPropertyChangeListener(connectedPlayersPanel);
        connectedPlayersPanel.updateListOfPlayers();
    }

    /**
     * Starts a thread listening for new connections.
     */
    private void prepareToHostNetworkGame(int port) throws IOException {
        if (isNewGame()) {
            initServer();
        }

        Thread thread = new Thread(() -> {
            BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
            try {
                ServerSocketAcceptor acceptor = new ServerSocketAcceptor(address, sockets);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    Socket socket = sockets.take();
                    Connection connection = Connection.make(socket);
                    server.addConnection(connection);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Server Connection Acceptor");
        thread.start();

        CardLayout cl = (CardLayout) jPanel1.getLayout();
        cl.show(jPanel1, "3");
        currentPage = 3;
    }


    private void prevButtonActionPerformed(ActionEvent evt) {
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
                LauncherPanel panel = (LauncherPanel) wizardPages[0];
                if (panel.getMode() == ClientConstants.MODE_JOIN_NETWORK_GAME) {
                    currentPage = 0;
                    cl.show(jPanel1, "0");
                    prevButton.setEnabled(false);
                } else {
                    currentPage--;
                    cl.previous(jPanel1);
                }
        }
    }

    private void nextButtonActionPerformed(ActionEvent evt) {
        try {
            CardLayout cl = (CardLayout) jPanel1.getLayout();
            LauncherPanel panel = (LauncherPanel) wizardPages[0];
            SelectMapPanel msp = (SelectMapPanel) wizardPages[1];
            ClientOptionsPanel cop = (ClientOptionsPanel) wizardPages[2];
            hideAllMessages();

            switch (currentPage) {
                case 0:
                    msp.validateInput();
                    // Initial game selection page
                    switch (panel.getMode()) {
                        case ClientConstants.MODE_SERVER_ONLY:
                            // go to map selection screen
                            cl.next(jPanel1);
                            msp.setServerPortPanelVisible(true);

                            currentPage++;
                            break;
                        case ClientConstants.MODE_START_NETWORK_GAME:
                            // go to map selection screen
                            msp.setServerPortPanelVisible(true);
                            cop.setRemoteServerPanelVisible(false);
                            cl.next(jPanel1);
                            currentPage++;
                            break;
                        case ClientConstants.MODE_JOIN_NETWORK_GAME:
                            // client display options
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
                    // map selection page
                    if (panel.getMode() == ClientConstants.MODE_SERVER_ONLY) {
                        if (msp.validateInput()) {
                            prevButton.setEnabled(false);
                            if (!isNewGame()) {
                                initServer();
                                server.loadGame("freerails.sav");
                            }
                            prepareToHostNetworkGame(msp.getServerPort());

                        }
                    } else {
                        if (isNewGame()) {
                            cop.limitPlayerNames(null);
                        } else {
                            initServer();
                            server.loadGame(msp.getSaveGameName());
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
                    // display mode selection
                    if (panel.getMode() == ClientConstants.MODE_START_NETWORK_GAME) {
                        if (msp.validateInput()) {
                            prevButton.setEnabled(false);
                            int mode = cop.getScreenMode();

                            prepareToHostNetworkGame(msp.getServerPort());
                            client = new LauncherClient(cop.getPlayerName(), mode, cop.getDisplayMode());
                            client.connect(cop.getPlayerName(), "password");
                        }
                    } else {

                        prevButton.setEnabled(false);
                        cop.setControlsEnabled(false);
                        startGame();
                    }
                    break;
                case 3:
                    try {
                        // Connection success screen
                        prevButton.setEnabled(false);
                        setServerGameModel();// TODO catch exception
                        if (panel.getMode() == ClientConstants.MODE_START_NETWORK_GAME) {
                            startThread(server, client);
                            cl.show(jPanel1, "4");
                        } else {
                            // Start a stand alone server.
                            startThread(server);
                            setVisible(false);
                        }
                        setButtonsVisible(false);
                        setNextEnabled(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setInfoText(e.getMessage(), InfoMessageType.WARNING);
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
            e.printStackTrace();
            emergencyStop();
        }
    }


    /**
     * @param text
     * @param status
     */

    @Override
    public void setInfoText(String text, InfoMessageType status) {
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
    @Override
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
    @Override
    public void hideErrorMessages() {
        if (infoLabel.getIcon() == errorIcon) {
            infoLabel.setText(null);
            infoLabel.setIcon(null);
            nextButton.setEnabled(true);
        }
    }
}
