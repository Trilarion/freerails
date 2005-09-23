/*
 * Launcher.java
 *
 * Created on 20 December 2003, 16:05
 */

package jfreerails.launcher;

import java.awt.CardLayout;
import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import jfreerails.client.top.GameLoop;
import jfreerails.controller.ReportBugTextGenerator;
import jfreerails.controller.ScreenHandler;
import jfreerails.controller.ServerControlInterface;
import jfreerails.network.FreerailsGameServer;
import jfreerails.network.InetConnectionAccepter;
import jfreerails.network.LogOnResponse;
import jfreerails.network.SavedGamesManager;
import jfreerails.server.SavedGamesManagerImpl;
import jfreerails.server.ServerGameModelImpl;
import jfreerails.util.GameModel;

/**
 * Launcher GUI for both the server and/or client.
 *
 * TODO The code in the switch statements needs reviewing.
 *
 * @author rtuck99@users.sourceforge.net
 * @author Luke
 */
public class Launcher extends javax.swing.JFrame implements LauncherInterface {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = Logger.getLogger(Launcher.class
            .getName());
    
    private static String QUICKSTART = "-quickstart";
    
    private final Component[] wizardPages = new Component[4];
    
    private int currentPage = 0;
    
    private FreerailsGameServer server;
    
    private GUIClient client;
    
    private Properties props;
    
    private final ImageIcon errorIcon = new javax.swing.ImageIcon(getClass()
    .getResource("/jfreerails/client/graphics/icons/error.gif"));
    
    private final ImageIcon warningIcon = new javax.swing.ImageIcon(getClass()
    .getResource("/jfreerails/client/graphics/icons/warning.gif"));
    
    private final ImageIcon infoIcon = new javax.swing.ImageIcon(getClass()
    .getResource("/jfreerails/client/graphics/icons/info.gif"));
    
    private final ProgressJPanel progressPanel = new ProgressJPanel(this);
    
    public void setNextEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        if (nextIsStart) {
            nextButton.setText("Start");
        } else {
            nextButton.setText("Next...");
        }
    }
    
    private boolean nextIsStart = false;
    
    private void startGame() {
        CardLayout cl = (CardLayout) jPanel1.getLayout();
        cl.show(jPanel1, "4");
        
        setButtonsVisible(false);
        LauncherPanel1 lp = (LauncherPanel1) wizardPages[0];
        SelectMapJPanel msp = (SelectMapJPanel) wizardPages[1];
        ClientOptionsJPanel cop = (ClientOptionsJPanel) wizardPages[2];
        ConnectedPlayersJPanel cp = (ConnectedPlayersJPanel) wizardPages[3];
        
        boolean recover = false;
        int mode;
        
        switch (lp.getMode()) {
            case LauncherPanel1.MODE_SINGLE_PLAYER:
                try {
                    
                    mode = cop.getScreenMode();
                    
                    client = new GUIClient(cop.getPlayerName(), progressPanel,
                            mode, cop.getDisplayMode());
                    if (isNewGame()) {
                        initServer();
                    }
                    client.connect(server, cop.getPlayerName(), "password");
                    
                    setServerGameModel();
                } catch (IOException e) {
                    setInfoText(e.getMessage(), LauncherInterface.WARNING);
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
                    playerNames = playerNames.length == 0 ? new String[] { "No players are connected." }
                    : playerNames;
                    cp.setListOfPlayers(playerNames);
                    cl.show(jPanel1, "3");
                    setNextEnabled(false);
                } catch (IOException e) {
                    // We end up here if an Exception was thrown when loading a
                    // saved game.
                    setInfoText(e.getMessage(), LauncherInterface.WARNING);
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
                    
                    InetSocketAddress serverInetAddress = cop
                            .getRemoteServerAddress();
                    if (null == serverInetAddress) {
                        throw new NullPointerException("Couldn't resolve hostname.");
                    }
                    String playerName = cop.getPlayerName();
                    client = new GUIClient(playerName, progressPanel, mode, cop
                            .getDisplayMode());
                    
                    String hostname = serverInetAddress.getHostName();
                    int port = serverInetAddress.getPort();
                    setInfoText("Connecting to server...", LauncherInterface.INFO);
                    LogOnResponse logOnResponse = client.connect(hostname, port,
                            playerName, "password");
                    if (logOnResponse.isSuccessful()) {                    	
                    	setInfoText("Logged on and waiting for game to start.", LauncherInterface.INFO);
                        startThread(client);
                    } else {
                        recover = true;
                        setInfoText(logOnResponse.getMessage(),
                                LauncherInterface.WARNING);
                    }
                } catch (IOException e) {
                    setInfoText(e.getMessage(), LauncherInterface.WARNING);
                    recover = true;
                } catch (NullPointerException e) {
                    setInfoText(e.getMessage(), LauncherInterface.WARNING);
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
                    } catch (NullPointerException e) {
                        setInfoText(e.getMessage(), LauncherInterface.WARNING);
                        recover = true;
                    } catch (IOException e) {
                        setInfoText(e.getMessage(), LauncherInterface.WARNING);
                        recover = true;
                    } finally {
                        if (recover) {
                            cop.setControlsEnabled(true);
                            prevButton.setEnabled(true);
                            setButtonsVisible(true);
                            return;
                        }
                    }
                    
                }
        }// End of switch statement
    }
    
    private void setServerGameModel() throws IOException {
        
        ClientOptionsJPanel cop = (ClientOptionsJPanel) wizardPages[2];
        if (isNewGame()) {
            SelectMapJPanel msp2 = (SelectMapJPanel) wizardPages[1];
            server.newGame(msp2.getNewMapName());
            cop.limitPlayerNames(null);
        } else {
            // Do nothing since the server is already set up.
        }
    }
    
    private boolean isNewGame() {
        SelectMapJPanel msp2 = (SelectMapJPanel) wizardPages[1];
        
        return msp2.getSelection().equals(SelectMapJPanel.Selection.NEW_GAME);
    }
    
    /** Starts the client and server in the same thread. */
	private static void startThread(final FreerailsGameServer server,
			final GUIClient client) {
		try {
			Runnable run = new Runnable() {

				public void run() {
					while (null == client.getWorld()) {
						client.update();
						server.update();
					}

					GameModel[] models = new GameModel[] { client, server };
					ScreenHandler screenHandler = client.getScreenHandler();
					GameLoop gameLoop = new GameLoop(screenHandler, models);
					screenHandler.apply();

					gameLoop.run();
				}

			};

			Thread t = new Thread(run, "Client + server main loop");
			t.start();
		} catch (Exception e) {
			exit(e);
		}
	}
    
    /** Starts the client in a new thread. */
    private void startThread(final GUIClient guiClient) {
    	try {
        Runnable run = new Runnable() {
            
            public void run() {
                while (null == guiClient.getWorld()) {
                    guiClient.update();                    
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }                
                GameModel[] models = new GameModel[] { guiClient };
                ScreenHandler screenHandler = guiClient.getScreenHandler();
                GameLoop gameLoop = new GameLoop(screenHandler, models);
                screenHandler.apply();
                
                gameLoop.run();
            }
            
        };
        
            Thread t = new Thread(run, "Client main loop");
            t.start();
        } catch (Exception e) {
            exit(e);
        }
    }
    
    /** Starts the server in a new thread. */
    private static void startThread(final FreerailsGameServer server) {
    	 try {
        Runnable r = new Runnable() {
            
            public void run() {
                
                while (true) {
                    long startTime = System.currentTimeMillis();
                    server.update();
                    long deltatime = System.currentTimeMillis() - startTime;
                    if (deltatime < 20) {
                        try {
                            Thread.sleep(20 - deltatime);
                        } catch (InterruptedException e) {
                            // do nothing.
                        }
                    }
                }
                
            }
            
        };
       
            
            Thread t = new Thread(r, "FreerailsGameServer");
            t.start();
        } catch (Exception e) {
            exit(e);
        }
    }
    
    private void initServer() {
        SavedGamesManager gamesManager = new SavedGamesManagerImpl();
        server = new FreerailsGameServer(gamesManager);
        ServerGameModelImpl serverGameModel = new ServerGameModelImpl();
        server.setServerGameModel(serverGameModel);
        
                /*
                 * Set the server field on the connected players panel so that it can
                 * keep track of who is connected.
                 */
        ConnectedPlayersJPanel cp = (ConnectedPlayersJPanel) wizardPages[3];
        cp.server = server;
        server.addPropertyChangeListener(cp);
        cp.updateListOfPlayers();
    }
    
    /**
     * Runs the game.
     */
    public static void main(String args[]) {
        
        // Let the user know if we are using a custom logging config.
        String loggingProperties = System
                .getProperty("java.util.logging.config.file");
        if (null != loggingProperties) {
            logger.info("Logging properties file: " + loggingProperties);
        }
        
        logger.fine("Started launcher.");
        boolean quickstart = false;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (QUICKSTART.equals(args[i]))
                    quickstart = true;
            }
            
        }
        Launcher launcher = new Launcher(quickstart);
        launcher.start(quickstart);
    }
    
    /**
     * Shows GUI. If <code>quickstart</code> is <code>true</code> runs the
     * game.
     *
     * @param quickstart
     *            boolean
     */
    public void start(boolean quickstart) {
        setVisible(true);
        if (quickstart) {
            startGame();
        }
    }
    
    /** Starts a thread listening for new connections. */
    private void prepare2HostNetworkGame(int port) throws IOException {
        loadProps();
        if (isNewGame()) {
            initServer();
        }
        InetConnectionAccepter accepter = new InetConnectionAccepter(port,
                server);
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
    
    public Launcher(boolean quickstart) {
        loadProps();
        initComponents();
        
        wizardPages[0] = new LauncherPanel1(this);
        wizardPages[1] = new SelectMapJPanel(this);
        wizardPages[2] = new ClientOptionsJPanel(this);
        wizardPages[3] = new ConnectedPlayersJPanel();
        
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {// GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        
        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();
        
        getContentPane().setLayout(new java.awt.GridBagLayout());
        
        setTitle("Freerails Launcher");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
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
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(nextButton, gridBagConstraints);
        
        prevButton.setText("Back...");
        prevButton.setEnabled(false);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        
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
    }// GEN-END:initComponents
    
    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_prevButtonActionPerformed
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
    }// GEN-LAST:event_prevButtonActionPerformed
    
    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_nextButtonActionPerformed
        try {
            CardLayout cl = (CardLayout) jPanel1.getLayout();
            LauncherPanel1 panel = (LauncherPanel1) wizardPages[0];
            SelectMapJPanel msp = (SelectMapJPanel) wizardPages[1];
            ClientOptionsJPanel cop = (ClientOptionsJPanel) wizardPages[2];
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
                                    server
                                            .loadgame(ServerControlInterface.FREERAILS_SAV);
                                }
                                prepare2HostNetworkGame(msp.getServerPort());
                            } catch (BindException be) {
                                // When the port is already in use.
                                prevButton.setEnabled(true);
                                setInfoText(be.getMessage(),
                                        LauncherInterface.WARNING);
                            }
                        }
                    } else {
                        if (isNewGame()) {
                            cop.limitPlayerNames(null);
                        } else {
                            initServer();
                            server.loadgame(msp.getSaveGameName());
                            String[] playernames = server.getPlayerNames();
                            cop.limitPlayerNames(playernames);
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
                            client = new GUIClient(cop.getPlayerName(),
                                    progressPanel, mode, cop.getDisplayMode());
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
                    } catch (IOException e) {
                        setInfoText(e.getMessage(), LauncherInterface.WARNING);
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
            exit(e);
        }
    }// GEN-LAST:event_nextButtonActionPerformed
    
    private static void exit(Exception e) {
       ReportBugTextGenerator.unexpectedException(e);
    }
    
    /** Exit the Application. */
    private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
        System.exit(0);
    }// GEN-LAST:event_exitForm
    
	// Variables declaration - do not modify//GEN-BEGIN:variables
	javax.swing.JLabel infoLabel;

	javax.swing.JPanel jPanel1;

	javax.swing.JButton nextButton;

	javax.swing.JButton prevButton;

	// End of variables declaration//GEN-END:variables
        
        public void setInfoText(String text, int status) {
            infoLabel.setText(text);
            switch (status) {
                case LauncherInterface.ERROR:
                    infoLabel.setIcon(errorIcon);
                    nextButton.setEnabled(false);
                    break;
                case LauncherInterface.INFO:
                    infoLabel.setIcon(infoIcon);
                    nextButton.setEnabled(true);
                    break;
                case LauncherInterface.WARNING:
                    infoLabel.setIcon(warningIcon);
                    nextButton.setEnabled(true);
                    break;
                default:
                    throw new IllegalArgumentException(String.valueOf(status));
            }
            
        }
        
        public void hideAllMessages() {
            infoLabel.setText(null);
            infoLabel.setIcon(null);
            nextButton.setEnabled(true);
            
        }
        
        public void setButtonsVisible(boolean b){
        	nextButton.setVisible(b);
        	prevButton.setVisible(b);
        }
        
        public void hideErrorMessages() {
            if (infoLabel.getIcon() == errorIcon) {
                infoLabel.setText(null);
                infoLabel.setIcon(null);
                nextButton.setEnabled(true);
            }
        }
        
        private void loadProps(){
            try{
                props = new Properties();
                FileInputStream in = new FileInputStream("freerails.properties");
                props.load(in);
                in.close();
                if(!props.containsKey("freerails.server.port") ||
                        !props.containsKey("freerails.server.port") ||
                        !props.containsKey("freerails.server.port")){
                    throw new Exception();
                }
            }catch (Exception e){
                props = new Properties();
                props.setProperty("freerails.server.port", "55000");
                props.setProperty("freerails.player.name", System.getProperty("user.name"));
                props.setProperty("freerails.server.ip.address", "127.0.0.1");
            }
        }
        
        public void saveProps(){
            try{
                FileOutputStream out = new FileOutputStream("freerails.properties");
                props.store(out, "---No Comment---");
                out.close();
                
                //Copy key-value pairs to System.Properties so
                //that they are visible in the game via the
                //show java properties menu item.
                System.getProperties().putAll(props);
                
            }catch (Exception e){
                logger.warning(e.getMessage());
            }
        }
        
        public  void setProperty(String key, String value){
            props.setProperty(key, value);
        }
        
        public String getProperty(String key){
            return props.getProperty(key);
        }
        
}
