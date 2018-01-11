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
import freerails.client.common.ModelRootImpl;
import freerails.client.common.MyGlassPanel;
import freerails.client.common.StationHelper;
import freerails.client.renderer.RendererRoot;
import freerails.network.MessageToServer;
import freerails.controller.ModelRoot.Property;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.RefreshListOfGamesMessageToServer;
import freerails.util.ImmutableList;
import freerails.world.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.TrainBlueprint;
import freerails.world.station.Station;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainTile;
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
    private static final Logger logger = Logger
            .getLogger(DialogueBoxController.class.getName());

    private final JButton closeButton = new JButton("Close");

    private final SelectEngineJPanel selectEngine;

    private final MyGlassPanel glassPanel;

    private final NewsPaperJPanel newspaper;

    private final SelectWagonsJPanel selectWagons;

    private final HtmlJPanel showControls;

    private final HtmlJPanel about;

    private final HtmlJPanel how2play;

    private final HtmlJPanel javaProperties;

    private final TerrainInfoJPanel terrainInfo;

    private final StationInfoJPanel stationInfo;

    private final TrainDialogueJPanel trainDialogueJPanel;
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
            WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, modelRoot
                    .getWorld(), modelRoot.getPrincipal());

            if (wi.next()) {
                Station station = (Station) wi.getElement();

                ImmutableList<TrainBlueprint> before = station.getProduction();
                int engineType = selectEngine.getEngineType();
                Integer[] wagonTypes = selectWagons.getWagons();
                ImmutableList<TrainBlueprint> after = new ImmutableList<>(
                        new TrainBlueprint(engineType, wagonTypes));

                Move m = new ChangeProductionAtEngineShopMove(before, after, wi
                        .getIndex(), modelRoot.getPrincipal());
                modelRoot.doMove(m);
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
        frame.getLayeredPane().addComponentListener(
                new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e) {
                        glassPanel.setSize(glassPanel.getParent().getSize());
                        glassPanel.revalidate();
                    }
                });

        closeButton.addActionListener(closeCurrentDialogue);

        showControls = new HtmlJPanel(DialogueBoxController.class
                .getResource(ClientConfig.VIEW_GAME_CONTROLS));
        about = new HtmlJPanel(DialogueBoxController.class
                .getResource(ClientConfig.VIEW_ABOUT));
        how2play = new HtmlJPanel(DialogueBoxController.class
                .getResource(ClientConfig.VIEW_HOW_TO_PLAY));

        terrainInfo = new TerrainInfoJPanel();
        stationInfo = new StationInfoJPanel();
        javaProperties = new HtmlJPanel(ShowJavaProperties
                .getPropertiesHtmlString());
        Dimension d = javaProperties.getPreferredSize();
        d.width += 50;
        javaProperties.setPreferredSize(d);
        newspaper = new NewsPaperJPanel();
        selectWagons = new SelectWagonsJPanel();
        selectEngine = new SelectEngineJPanel();
        trainDialogueJPanel = new TrainDialogueJPanel();
        /*
         * 666 needed ?? trainDialogueJPanel.setIgnoreRepaint(true);
         * selectEngine.setIgnoreRepaint(true);
         * selectWagons.setIgnoreRepaint(true);
         */

    }

    /**
     * Called when a new game is started or a game is loaded.
     *
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     *
     * @param mr
     * @param vl
     */
    public void setup(ModelRootImpl mr, RendererRoot vl) {
        modelRoot = mr;
        this.vl = vl;
        modelRoot.addListListener(this); // When a new train gets built, we
        // show the train info etc

        world = modelRoot.getWorld();

        if (world == null)
            throw new NullPointerException();

        if (vl == null)
            throw new NullPointerException();

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

        // Set up train orders dialogue
        // trainScheduleJPanel = new TrainScheduleJPanel();
        // trainScheduleJPanel.setup(w, vl);
        // moveChainFork.add(trainScheduleJPanel);
        // Set up select engine dialogue.
        selectEngine.setCancelButtonActionListener(closeCurrentDialogue);
        selectEngine.setup(modelRoot, vl, selectEngineAction);

        newspaper.setup(modelRoot, vl, closeCurrentDialogue);

        selectWagons.setup(modelRoot, vl, selectWagonsAction);

        trainDialogueJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        modelRoot.addListListener(trainDialogueJPanel);
        trainDialogueJPanel
                .setTrainDetailsButtonActionListener(trainDetailsButtonActionListener);
        trainDialogueJPanel
                .setCancelButtonActionListener(closeCurrentDialogue);
    }

    /**
     *
     */
    public void showSaveGame() {
        SaveGameJPanel saveGameJPanel = new SaveGameJPanel();
        saveGameJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(saveGameJPanel);
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
        WorldIterator wi = new NonNullElementWorldIterator(KEY.TRAINS, world, modelRoot
                .getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Cannot"
                    + " show train orders since there are no" + " trains!");
        } else {
            trainDialogueJPanel.display(wi.getIndex());
            showContent(trainDialogueJPanel);
        }
    }

    /**
     *
     */
    public void showSelectEngine() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, world, modelRoot
                .getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Can't"
                    + " build train since there are no stations");
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
        IncomeStatementHtmlJPanel bs = new IncomeStatementHtmlJPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     *
     */
    public void showBalanceSheet() {
        BalanceSheetHtmlJPanel bs = new BalanceSheetHtmlJPanel();
        bs.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(bs);
    }

    /**
     *
     */
    public void showBrokerScreen() {
        // this is Creating a BrokerScreen Internal Frame in the Main Frame
        BrokerScreenHtmlJFrame brokerScreenHtmlJFrame = new BrokerScreenHtmlJFrame();
        brokerScreenHtmlJFrame.setup(modelRoot, vl,
                closeCurrentDialogue);
        brokerScreenHtmlJFrame.setFrameIcon(null);

        showContent(brokerScreenHtmlJFrame);
    }

    // Shows the Exit Dialog

    /**
     *
     */
    public void showExitDialog() {
        ConfirmExitJPanel bs = new ConfirmExitJPanel();
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
    public void showTerrainInfo(int x, int y) {
        TerrainTile tile = (FullTerrainTile) world.getTile(x, y);
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
            trainDialogueJPanel.display(trainId);
            showContent(trainDialogueJPanel);
        }
    }

    /**
     *
     */
    public void showTrainList() {
        if (world.size(modelRoot.getPrincipal(), KEY.TRAINS) > 0) {
            final TrainListJPanel trainList = new TrainListJPanel();
            trainList.setup(modelRoot, vl, closeCurrentDialogue);
            trainList.setShowTrainDetailsActionListener(e -> {
                int id = trainList.getSelectedTrainID();
                showTrainOrders(id);
            });

            showContent(trainList);
        } else {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "There are"
                    + " no trains to display!");
        }
    }

    /**
     *
     */
    public void showNetworthGraph() {

        final NetWorthGraphJPanel worthGraph = new NetWorthGraphJPanel();
        worthGraph.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(worthGraph);

    }

    /**
     *
     */
    public void showLeaderBoard() {

        LeaderBoardJPanel leaderBoardJPanel = new LeaderBoardJPanel();
        leaderBoardJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(leaderBoardJPanel);

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

        dialogueJInternalFrame.setLocation(
                (frame.getWidth() - dialogueJInternalFrame.getWidth()) / 2,
                (frame.getHeight() - dialogueJInternalFrame.getHeight()) / 2);

        frame.getLayeredPane().add(dialogueJInternalFrame,
                JLayeredPane.MODAL_LAYER);

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
    public void showStationOrTerrainInfo(int x, int y) {
        int stationNumberAtLocation = StationHelper.getStationNumberAtLocation(world, modelRoot, x, y);
        if (stationNumberAtLocation > -1) {
            showStationInfo(stationNumberAtLocation);
        } else {
            showTerrainInfo(x, y);
        }


    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        // do nothing
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
        boolean rightPrincipal = principal
                .equals(modelRoot.getPrincipal());

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
        // do nothing
    }
}