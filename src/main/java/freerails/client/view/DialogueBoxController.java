/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.operations.station.StationUtils;
import freerails.model.building.EngineShop;
import freerails.model.operations.train.Train;
import freerails.model.world.*;
import freerails.util.ui.MyGlassPanel;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRootProperty;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.BuildEngineShopMove;
import freerails.move.Move;
import freerails.network.command.CommandToServer;
import freerails.network.command.RefreshListOfGamesCommandToServer;

import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.terrain.TerrainTile;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
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
public class DialogueBoxController {

    private static final TaggedLogger logger = Logger.tag("DialogueBoxController");
    private final JButton closeButton = new JButton("Close");
    private final SelectEnginePanel selectEngine;
    private final MyGlassPanel glassPanel;
    private final NewsPaperPanel newspaper;
    private final SelectWagonsPanel selectWagons;
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

        @Serial
        private static final long serialVersionUID = 673348332616193928L;

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            closeContent();
        }
    };

    private final Action selectEngineAction = new AbstractAction() {

        @Serial
        private static final long serialVersionUID = -5932587740749235632L;

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            showSelectWagons();
        }
    };

    private final ActionListener trainDetailsButtonActionListener = e -> showTrainList();

    private final Action selectWagonsAction = new AbstractAction("Next") {

        @Serial
        private static final long serialVersionUID = -1672545312581874156L;

        /**
         * Handles the specified action event.
         * @param e the e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            int engineId = selectEngine.getSelectedEngineId();
            int engineShopId = selectEngine.getSelectedEngineShopId();
            List<Integer> wagonTypes = selectWagons.getWagons();
            List<TrainTemplate> after = List.of(new TrainTemplate(engineId, wagonTypes));
            Move move = new ChangeProductionAtEngineShopMove(after, engineShopId, modelRoot.getPlayer());
            modelRoot.applyMove(move);
            closeContent();
        }
    };

    /**
     * Creates a dialogue box controller instance.
     *
     * @param frame the frame parameter
     * @param mr the mr parameter
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
            /**
             * Component resized.
             * @param e the e
             */
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                glassPanel.setSize(glassPanel.getParent().getSize());
                glassPanel.revalidate();
            }
        });

        closeButton.addActionListener(closeCurrentDialogue);

        terrainInfo = new TerrainInfoPanel();
        stationInfo = new StationInfoPanel();
        newspaper = new NewsPaperPanel();
        selectWagons = new SelectWagonsPanel();
        selectEngine = new SelectEnginePanel();
        trainDialoguePanel = new TrainDialoguePanel();
    }

    /**
     * Called when a new game is started or a game is loaded.
     * <p>
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     */
    public void setup(ModelRootImpl mr, RendererRoot vl) {
        modelRoot = mr;
        this.vl = Utils.verifyNotNull(vl);
        // TODO not notified of changes in the world anymore
        // When a new train gets built, we show the train info etc

        world = modelRoot.getWorld();
        Utils.verifyNotNull(world);

        // Setup the various dialogue boxes.

        // setup the terrain info dialogue.
        terrainInfo.setup(world, vl);

        // setup the supply and demand at station dialogue.
        stationInfo.setup(modelRoot, vl, closeCurrentDialogue);
        // Set up select engine dialogue.
        selectEngine.setCancelButtonActionListener(closeCurrentDialogue);
        selectEngine.setup(modelRoot, vl, selectEngineAction);

        newspaper.setup(modelRoot, vl, closeCurrentDialogue);

        selectWagons.setup(modelRoot, vl, selectWagonsAction);

        trainDialoguePanel.setup(modelRoot, vl, closeCurrentDialogue);
        trainDialoguePanel.setTrainDetailsButtonActionListener(trainDetailsButtonActionListener);
        trainDialoguePanel.setCancelButtonActionListener(closeCurrentDialogue);
    }

    /**
     * Displays the save game.
     */
    public void showSaveGame() {
        SaveGamePanel saveGamePanel = new SaveGamePanel();
        saveGamePanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(saveGamePanel);
    }

    /**
     * Displays the select saved game2 load.
     */
    public void showSelectSavedGame2Load() {
        CommandToServer refreshGames = new RefreshListOfGamesCommandToServer();
        modelRoot.sendCommand(refreshGames);
        LoadGamePanel loadGameJPane = new LoadGamePanel();
        loadGameJPane.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(loadGameJPane);
    }

    /**
     * Displays the train orders.
     */
    public void showTrainOrders() {
        Collection<Train> trains = world.getTrains(modelRoot.getPlayer());

        if (trains.isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Cannot" + " show train orders since there are no" + " trains!");
        } else {
            // TODO there should be something reasonable here, like the next and so on, but I don't know what this is supposed to do
            trainDialoguePanel.display(0);
            showContent(trainDialoguePanel);
        }
    }

    /**
     * Displays the select engine.
     */
    public void showSelectEngine() {
        if (world.getStations(modelRoot.getPlayer()).isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Can't" + " build train since there are no stations");
        } else if (world.getEngineShops(modelRoot.getPlayer()).isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE,
                    "Build an engine shop at a station before building trains.");
        } else {
            selectEngine.setEngineShops(world.getEngineShops(modelRoot.getPlayer()));
            showContent(selectEngine);
        }
    }

    /** Opens the station selector and builds an engine shop at the chosen station. */
    public void showBuildEngineShop() {
        List<Station> availableStations = world.getStations(modelRoot.getPlayer()).stream()
                .filter(station -> world.getEngineShops(modelRoot.getPlayer()).stream()
                        .noneMatch(shop -> shop.getLocation().equals(station.getLocation())))
                .toList();
        if (availableStations.isEmpty()) {
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE,
                    "Build a station first, or all stations already have an engine shop.");
            return;
        }

        String[] stationOptions = availableStations.stream()
                .map(station -> station.getStationName() + " (" + station.getLocation().x()
                        + ", " + station.getLocation().y() + ")")
                .toArray(String[]::new);
        Object selectedStation = JOptionPane.showInputDialog(frame,
                "Choose a station for the engine shop. Construction cost: " + BuildEngineShopMove.BUILD_COST,
                "Build Engine Shop", JOptionPane.PLAIN_MESSAGE, null, stationOptions, stationOptions[0]);
        if (selectedStation == null) {
            return;
        }

        int selectedIndex = Arrays.asList(stationOptions).indexOf(selectedStation);
        Station station = availableStations.get(selectedIndex);
        int nextId = world.getEngineShops(modelRoot.getPlayer()).stream()
                .mapToInt(EngineShop::getId)
                .max()
                .orElse(-1) + 1;
        Move move = new BuildEngineShopMove(modelRoot.getPlayer(),
                new EngineShop(nextId, station.getLocation()));
        modelRoot.applyMove(move);
    }

    /**
     * Displays the income statement.
     */
    public void showIncomeStatement() {
        IncomeStatementHtmlPanel bs = new IncomeStatementHtmlPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     * Displays the balance sheet.
     */
    public void showBalanceSheet() {
        BalanceSheetHtmlPanel bs = new BalanceSheetHtmlPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     * Displays the broker screen.
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
     * Displays the exit dialog.
     */
    public void showExitDialog() {
        ConfirmExitPanel bs = new ConfirmExitPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     * Displays the select wagons.
     */
    public void showSelectWagons() {
        selectWagons.resetSelectedWagons();
        selectWagons.setEngineId(selectEngine.getSelectedEngineId());
        showContent(selectWagons);
    }

    /**
     * Displays the terrain info.
     *
     * @param terrainType the terrain type parameter
     */
    public void showTerrainInfo(int terrainType) {
        terrainInfo.setTerrainType(terrainType);
        showContent(terrainInfo);
    }

    /**
     * Displays the terrain info.
     *
     * @param p the map coordinate
     */
    private void showTerrainInfo(Vector2D p) {
        TerrainTile tile = world.getTile(p);
        int terrainType = tile.terrainTypeId();
        showTerrainInfo(terrainType);
    }

    /**
     * Displays the station info.
     *
     * @param stationNumber the station number parameter
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
     * Displays the train orders.
     *
     * @param trainId the train id parameter
     */
    public void showTrainOrders(int trainId) {
        closeContent();

        if (trainId != -1) {
            trainDialoguePanel.display(trainId);
            showContent(trainDialoguePanel);
        }
    }

    /**
     * Displays the train list.
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
     * Displays the net worth graph.
     */
    public void showNetWorthGraph() {

        final NetWorthGraphPanel worthGraph = new NetWorthGraphPanel();
        worthGraph.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(worthGraph);
    }

    /**
     * Displays the leader board.
     */
    public void showLeaderBoard() {

        LeaderBoardPanel leaderBoardPanel = new LeaderBoardPanel();
        leaderBoardPanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(leaderBoardPanel);
    }

    /**
     * Displays the content.
     *
     * @param component the component parameter
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
     * Performs the close content operation.
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
     * Sets the default focus owner.
     *
     * @param defaultFocusOwner the default focus owner parameter
     */
    public void setDefaultFocusOwner(Component defaultFocusOwner) {
        this.defaultFocusOwner = defaultFocusOwner;
    }

    /**
     * Displays the station or terrain info.
     *
     * @param p the map coordinate
     */
    public void showStationOrTerrainInfo(Vector2D p) {
        int stationNumberAtLocation = StationUtils.getStationIdAtLocation(world, modelRoot.getPlayer(), p);
        if (stationNumberAtLocation > -1) {
            showStationInfo(stationNumberAtLocation);
        } else {
            showTerrainInfo(p);
        }

    }

    /**
     * Performs the item added operation.
     *
     * @param index the index of the item
     * @param player the player whose game state is used
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
}