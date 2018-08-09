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

import freerails.client.ClientConstants;
import freerails.client.ModelRootImpl;
import freerails.model.train.Train;
import freerails.model.world.*;
import freerails.util.ui.MyGlassPanel;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRootProperty;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.command.CommandToServer;
import freerails.network.command.RefreshListOfGamesCommandToServer;

import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.station.TrainBlueprint;
import freerails.model.terrain.TerrainTile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

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
    private final TerrainInfoPanel terrainInfo;
    private final StationInfoPanel stationInfo;
    private final TrainDialoguePanel trainDialoguePanel;
    private final JFrame frame;
    private UnmodifiableWorld world;
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
            for (Station station: world.getStations(modelRoot.getPlayer())) {
                List<TrainBlueprint> before = station.getProduction();
                int engineId = selectEngine.getSelectedEngineId();
                Integer[] wagonTypes = selectWagons.getWagons();
                List<TrainBlueprint> after = Arrays.asList(new TrainBlueprint(engineId, wagonTypes));

                Move move = new ChangeProductionAtEngineShopMove(before, after, station.getId(), modelRoot.getPlayer());
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

        showControls = new HtmlPanel(DialogueBoxController.class.getResource(ClientConstants.VIEW_GAME_CONTROLS));
        about = new HtmlPanel(DialogueBoxController.class.getResource(ClientConstants.VIEW_ABOUT));
        how2play = new HtmlPanel(DialogueBoxController.class.getResource(ClientConstants.VIEW_HOW_TO_PLAY));

        terrainInfo = new TerrainInfoPanel();
        stationInfo = new StationInfoPanel();
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
        CommandToServer refreshGames = new RefreshListOfGamesCommandToServer();
        modelRoot.sendCommand(refreshGames);
        LoadGamePanel loadGameJPane = new LoadGamePanel();
        loadGameJPane.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(loadGameJPane);
    }

    /**
     *
     */
    public void showTrainOrders() {
        Collection<Train> trains = world.getTrains(modelRoot.getPlayer());

        if (trains.isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Cannot" + " show train orders since there are no" + " trains!");
        } else {
            // TODO there should be something reasonable here, like the next and so one, but I don't know what this is supposed to do
            trainDialoguePanel.display(0);
            showContent(trainDialoguePanel);
        }
    }

    /**
     *
     */
    public void showSelectEngine() {
        if (world.getStations(modelRoot.getPlayer()).isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Can't" + " build train since there are no stations");
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
    public void showSelectWagons() {
        selectWagons.resetSelectedWagons();
        selectWagons.setEngineId(selectEngine.getSelectedEngineId());
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
     * @param p
     */
    private void showTerrainInfo(Vec2D p) {
        TerrainTile tile = (TerrainTile) world.getTile(p);
        int terrainType = tile.getTerrainTypeId();
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
        if (!world.getTrains(modelRoot.getPlayer()).isEmpty()) {
            final TrainListPanel trainList = new TrainListPanel();
            trainList.setup(modelRoot, vl, closeCurrentDialogue);
            trainList.setShowTrainDetailsActionListener(e -> {
                int id = trainList.getSelectedTrainID();
                showTrainOrders(id);
            });

            showContent(trainList);
        } else {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "There are" + " no trains to display!");
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
     * @param p
     */
    public void showStationOrTerrainInfo(Vec2D p) {
        int stationNumberAtLocation = Station.getStationIdAtLocation(world, modelRoot.getPlayer(), p);
        if (stationNumberAtLocation > -1) {
            showStationInfo(stationNumberAtLocation);
        } else {
            showTerrainInfo(p);
        }

    }

    /**
     * @param index
     * @param player
     */
    public void listUpdated(int index, Player player) {
    }

    /**
     * @param index
     * @param player
     */
    public void itemAdded(int index, Player player) {
        /*
         * Fix for: 910138 After building a train display train orders 910143
         * After building station show supply and demand
         */
        boolean rightPlayer = player.equals(modelRoot.getPlayer());

        // TODO after newly created train with AddTrainMove, this is not done right now
        if (rightPlayer) {
            //showTrainOrders(index);
        }

        // TODO when is this called? when a station is added?
        if (rightPlayer) {
            showStationInfo(index);
        }
    }

    /**
     * @param index
     * @param player
     */
    public void itemRemoved(int index, Player player) {
    }
}