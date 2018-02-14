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
 * DialogueBoxController.java
 *
 */
package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.ModelRootImpl;
import freerails.util.ui.MyGlassPanel;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot.Property;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.message.MessageToServer;
import freerails.network.message.RefreshListOfGamesMessageToServer;
import freerails.util.ImmutableList;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.world.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.station.TrainBlueprint;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainTile;
import freerails.world.world.ReadOnlyWorld;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;

/**
 * Is responsible for displaying dialogue boxes, adding borders to
 * them as appropriate, and returning focus to the last focus owner after a
 * dialogue box has been closed. It is also responsible for adding components
 * that need to update in response to moves to the MoveChainFork. Currently
 * dialogue boxes are not separate windows. Instead, they are drawn on the modal
 * layer of the main JFrames LayerPlane. This allows dialogue boxes with
 * transparent regions to be used.
 */
public class DialogueBoxController implements WorldListListener {

    private static final Logger logger = Logger.getLogger(DialogueBoxController.class.getName());
    private final JButton closeButton = new JButton("Close");
    private final SelectEnginePanel selectEngine;
    private final MyGlassPanel glassPanel;
    private final NewsPaperPanel newspaper;
    private final SelectWagonsPanel selectWagons;
    private final HtmlPanel showControls;
    private final HtmlPanel about;
    private final HtmlPanel how2play;
    private final HtmlPanel javaProperties;
    private final TerrainInfoPanel terrainInfo;
    private final StationInfoPanel stationInfo;
    private final TrainDialoguePanel trainDialoguePanel;
    private final JFrame frame;
    private ReadOnlyWorld world;
    private ModelRootImpl modelRoot;
    private RendererRoot vl;
    private Component defaultFocusOwner = null;
    private JInternalFrame dialogueJInternalFrame;
    private Component lastShownComponent = null;

    /**
     * Use this Action to close a dialogue without performing any other action.
     */
    private final Action closeCurrentDialogue = new AbstractAction("Close") {

        private static final long serialVersionUID = 673348332616193928L;

        public void actionPerformed(ActionEvent e) {
            closeContent();
        }
    };

    private final Action selectEngineAction = new AbstractAction() {

        private static final long serialVersionUID = -5932587740749235632L;

        public void actionPerformed(ActionEvent e) {
            showSelectWagons();
        }
    };

    private final ActionListener trainDetailsButtonActionListener = e -> showTrainList();

    private final Action selectWagonsAction = new AbstractAction("Next") {

        private static final long serialVersionUID = -1672545312581874156L;

        public void actionPerformed(ActionEvent e) {
            WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, modelRoot.getWorld(), modelRoot.getPrincipal());

            if (wi.next()) {
                Station station = (Station) wi.getElement();

                ImmutableList<TrainBlueprint> before = station.getProduction();
                int engineType = selectEngine.getEngineType();
                Integer[] wagonTypes = selectWagons.getWagons();
                ImmutableList<TrainBlueprint> after = new ImmutableList<>(new TrainBlueprint(engineType, wagonTypes));

                Move move = new ChangeProductionAtEngineShopMove(before, after, wi.getIndex(), modelRoot.getPrincipal());
                modelRoot.doMove(move);
            }
            closeContent();
        }
    };

    /**
     * @param frame
     * @param mr
     */
    public DialogueBoxController(JFrame frame, ModelRootImpl mr) {
        this.frame = frame;
        modelRoot = mr;

        // Setup glass panel..
        glassPanel = new MyGlassPanel();
        glassPanel.setSize(frame.getSize());
        frame.getLayeredPane().add(glassPanel, JLayeredPane.MODAL_LAYER);
        glassPanel.revalidate();
        glassPanel.setVisible(false);

        // We need to resize the glass panel when its parent resizes.
        frame.getLayeredPane().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                glassPanel.setSize(glassPanel.getParent().getSize());
                glassPanel.revalidate();
            }
        });

        closeButton.addActionListener(closeCurrentDialogue);

        showControls = new HtmlPanel(DialogueBoxController.class.getResource(ClientConfig.VIEW_GAME_CONTROLS));
        about = new HtmlPanel(DialogueBoxController.class.getResource(ClientConfig.VIEW_ABOUT));
        how2play = new HtmlPanel(DialogueBoxController.class.getResource(ClientConfig.VIEW_HOW_TO_PLAY));

        terrainInfo = new TerrainInfoPanel();
        stationInfo = new StationInfoPanel();
        javaProperties = new HtmlPanel(ShowJavaProperties.getPropertiesHtmlString());
        Dimension d = javaProperties.getPreferredSize();
        d.width += 50;
        javaProperties.setPreferredSize(d);
        newspaper = new NewsPaperPanel();
        selectWagons = new SelectWagonsPanel();
        selectEngine = new SelectEnginePanel();
        trainDialoguePanel = new TrainDialoguePanel();
    }

    /**
     * Called when a new game is started or a game is loaded.
     *
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     */
    public void setup(ModelRootImpl mr, RendererRoot vl) {
        modelRoot = mr;
        this.vl = Utils.verifyNotNull(vl);
        modelRoot.addListListener(this); // When a new train gets built, we
        // show the train info etc

        world = modelRoot.getWorld();
        Utils.verifyNotNull(world);

        // Setup the various dialogue boxes.

        // setup the terrain info dialogue.
        terrainInfo.setup(world, vl);

        // setup the supply and demand at station dialogue.
        stationInfo.setup(modelRoot, vl, closeCurrentDialogue);
        modelRoot.addListListener(stationInfo);

        // setup the 'show controls' dialogue
        showControls.setup(modelRoot, vl, closeCurrentDialogue);
        about.setup(modelRoot, vl, closeCurrentDialogue);
        how2play.setup(modelRoot, vl, closeCurrentDialogue);
        javaProperties.setup(modelRoot, vl, closeCurrentDialogue);

        // Set up select engine dialogue.
        selectEngine.setCancelButtonActionListener(closeCurrentDialogue);
        selectEngine.setup(modelRoot, vl, selectEngineAction);

        newspaper.setup(modelRoot, vl, closeCurrentDialogue);

        selectWagons.setup(modelRoot, vl, selectWagonsAction);

        trainDialoguePanel.setup(modelRoot, vl, closeCurrentDialogue);
        modelRoot.addListListener(trainDialoguePanel);
        trainDialoguePanel.setTrainDetailsButtonActionListener(trainDetailsButtonActionListener);
        trainDialoguePanel.setCancelButtonActionListener(closeCurrentDialogue);
    }

    /**
     *
     */
    public void showSaveGame() {
        SaveGamePanel saveGamePanel = new SaveGamePanel();
        saveGamePanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(saveGamePanel);
    }

    /**
     *
     */
    public void showSelectSavedGame2Load() {
        MessageToServer refreshGames = new RefreshListOfGamesMessageToServer(2);
        modelRoot.sendCommand(refreshGames);
        LoadGameJPanel loadGameJPane = new LoadGameJPanel();
        loadGameJPane.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(loadGameJPane);
    }

    /**
     *
     */
    public void showTrainOrders() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.TRAINS, world, modelRoot.getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Cannot" + " show train orders since there are no" + " trains!");
        } else {
            trainDialoguePanel.display(wi.getIndex());
            showContent(trainDialoguePanel);
        }
    }

    /**
     *
     */
    public void showSelectEngine() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, world, modelRoot.getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Can't" + " build train since there are no stations");
        } else {
            showContent(selectEngine);
        }
    }

    /**
     *
     */
    public void showGameControls() {
        showContent(showControls);
    }

    /**
     *
     */
    public void showIncomeStatement() {
        IncomeStatementHtmlPanel bs = new IncomeStatementHtmlPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     *
     */
    public void showBalanceSheet() {
        BalanceSheetHtmlPanel bs = new BalanceSheetHtmlPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     *
     */
    public void showBrokerScreen() {
        // this is Creating a BrokerScreen Internal Frame in the Main Frame
        BrokerScreenHtmlFrame brokerScreenHtmlJFrame = new BrokerScreenHtmlFrame();
        brokerScreenHtmlJFrame.setup(modelRoot, vl, closeCurrentDialogue);
        brokerScreenHtmlJFrame.setFrameIcon(null);

        showContent(brokerScreenHtmlJFrame);
    }

    // Shows the Exit Dialog

    /**
     *
     */
    public void showExitDialog() {
        ConfirmExitPanel bs = new ConfirmExitPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     *
     */
    public void showAbout() {
        showContent(about);
    }

    /**
     *
     */
    public void showHow2Play() {
        showContent(how2play);
    }

    /**
     *
     */
    public void showJavaProperties() {
        showContent(javaProperties);
    }

    /**
     *
     */
    public void showSelectWagons() {
        selectWagons.resetSelectedWagons();
        selectWagons.setEngineType(selectEngine.getEngineType());
        showContent(selectWagons);
    }

    /**
     * @param terrainType
     */
    public void showTerrainInfo(int terrainType) {
        terrainInfo.setTerrainType(terrainType);
        showContent(terrainInfo);
    }

    /**
     * @param x
     * @param y
     */
    private void showTerrainInfo(Vector2D p) {
        TerrainTile tile = (FullTerrainTile) world.getTile(p);
        int terrainType = tile.getTerrainTypeID();
        showTerrainInfo(terrainType);
    }

    /**
     * @param stationNumber
     */
    public void showStationInfo(int stationNumber) {
        try {
            stationInfo.setStation(stationNumber);
            showContent(stationInfo);
        } catch (NoSuchElementException e) {
            logger.warn("Station " + stationNumber + " does not exist!");
        }
    }

    /**
     * @param trainId
     */
    public void showTrainOrders(int trainId) {
        closeContent();

        if (trainId != -1) {
            trainDialoguePanel.display(trainId);
            showContent(trainDialoguePanel);
        }
    }

    /**
     *
     */
    public void showTrainList() {
        if (world.size(modelRoot.getPrincipal(), KEY.TRAINS) > 0) {
            final TrainListPanel trainList = new TrainListPanel();
            trainList.setup(modelRoot, vl, closeCurrentDialogue);
            trainList.setShowTrainDetailsActionListener(e -> {
                int id = trainList.getSelectedTrainID();
                showTrainOrders(id);
            });

            showContent(trainList);
        } else {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "There are" + " no trains to display!");
        }
    }

    /**
     *
     */
    public void showNetworthGraph() {

        final NetWorthGraphPanel worthGraph = new NetWorthGraphPanel();
        worthGraph.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(worthGraph);
    }

    /**
     *
     */
    public void showLeaderBoard() {

        LeaderBoardPanel leaderBoardPanel = new LeaderBoardPanel();
        leaderBoardPanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(leaderBoardPanel);
    }

    /**
     * @param component
     */
    public void showContent(JComponent component) {
        closeContent();
        JComponent contentPanel;

        if (component instanceof JInternalFrame) {
            dialogueJInternalFrame = (JInternalFrame) component;
        } else {
            if (!(component instanceof View)) {
                contentPanel = new javax.swing.JPanel();
                contentPanel.setLayout(new java.awt.GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.insets = new Insets(7, 7, 7, 7);
                contentPanel.add(component, constraints);

                constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.insets = new Insets(7, 7, 7, 7);
                contentPanel.add(closeButton, constraints);
            } else {
                contentPanel = component;
            }
            lastShownComponent = component;
            lastShownComponent.setVisible(true);
            dialogueJInternalFrame = new JInternalFrame();
            dialogueJInternalFrame.setFrameIcon(null);
            dialogueJInternalFrame.getContentPane().add(contentPanel);
            dialogueJInternalFrame.pack();
        }

        /*
         * Make sure the size of the dialogue does not exceed the size of the
         * frames content pane.
         */
        int parentWidth = frame.getContentPane().getWidth();
        int parentHeight = frame.getContentPane().getHeight();

        Dimension size = dialogueJInternalFrame.getSize();

        if (size.width > parentWidth) {
            size.width = parentWidth;
        }

        if (size.height > parentHeight) {
            size.height = parentHeight;
        }

        dialogueJInternalFrame.setSize(size);

        dialogueJInternalFrame.setLocation((frame.getWidth() - dialogueJInternalFrame.getWidth()) / 2, (frame.getHeight() - dialogueJInternalFrame.getHeight()) / 2);

        frame.getLayeredPane().add(dialogueJInternalFrame, JLayeredPane.MODAL_LAYER);

        dialogueJInternalFrame.setVisible(true);
    }

    /**
     *
     */
    public void closeContent() {
        if (null != dialogueJInternalFrame) {
            dialogueJInternalFrame.setVisible(false);
            frame.getLayeredPane().remove(dialogueJInternalFrame);
            dialogueJInternalFrame.dispose();
            if (lastShownComponent != null) {
                lastShownComponent.setVisible(false);
                lastShownComponent = null;
            }
            dialogueJInternalFrame = null;
        }

        if (null != defaultFocusOwner) {
            defaultFocusOwner.requestFocus();
        }
    }

    /**
     * @param defaultFocusOwner
     */
    public void setDefaultFocusOwner(Component defaultFocusOwner) {
        this.defaultFocusOwner = defaultFocusOwner;
    }

    /**
     * @param x
     * @param y
     */
    public void showStationOrTerrainInfo(Vector2D p) {
        int stationNumberAtLocation = Station.getStationNumberAtLocation(world, modelRoot.getPrincipal(), p);
        if (stationNumberAtLocation > -1) {
            showStationInfo(stationNumberAtLocation);
        } else {
            showTerrainInfo(p);
        }

    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
        /*
         * Fix for: 910138 After building a train display train orders 910143
         * After building station show supply and demand
         */
        boolean rightPrincipal = principal.equals(modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            showTrainOrders(index);
        } else if (KEY.STATIONS == key && rightPrincipal) {
            showStationInfo(index);
        }
    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
    }
}