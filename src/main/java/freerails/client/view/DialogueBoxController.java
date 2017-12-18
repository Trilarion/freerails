/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */
package freerails.client.view;

import freerails.client.common.ModelRootImpl;
import freerails.client.common.MyGlassPanel;
import freerails.client.common.StationHelper;
import freerails.client.renderer.RenderersRoot;
import freerails.config.ClientConfig;
import freerails.controller.CopyableTextJPanel;
import freerails.controller.Message2Server;
import freerails.controller.ModelRoot.Property;
import freerails.controller.ReportBugTextGenerator;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.RefreshListOfGamesMessage2Server;
import freerails.world.common.ImList;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.PlannedTrain;
import freerails.world.station.StationModel;
import freerails.world.top.*;
import freerails.world.track.FreerailsTile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;

/**
 * This class is responsible for displaying dialogue boxes, adding borders to
 * them as appropriate, and returning focus to the last focus owner after a
 * dialogue box has been closed. It is also responsible for adding components
 * that need to update in response to moves to the MoveChainFork. Currently
 * dialogue boxes are not separate windows. Instead, they are drawn on the modal
 * layer of the main JFrames LayerPlane. This allows dialogue boxes with
 * transparent regions to be used.
 *
 * @author lindsal8
 * @author smackay
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
    private RenderersRoot vl;
    private Component defaultFocusOwner = null;
    private JInternalFrame dialogueJInternalFrame;

    private Component lastShownComponent = null;

    /**
     * Use this Action to close a dialogue without performing any other action.
     */
    private final Action closeCurrentDialogue = new AbstractAction("Close") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent arg0) {
            closeContent();
        }
    };

    private final Action selectEngineAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent arg0) {
            showSelectWagons();
        }
    };

    private final ActionListener trainDetailsButtonActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            showTrainList();
        }
    };

    private final Action selectWagonsAction = new AbstractAction("Next") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent arg0) {
            WorldIterator wi = new NonNullElements(KEY.STATIONS, modelRoot
                    .getWorld(), modelRoot.getPrincipal());

            if (wi.next()) {
                StationModel station = (StationModel) wi.getElement();

                ImList<PlannedTrain> before = station.getProduction();
                int engineType = selectEngine.getEngineType();
                int[] wagonTypes = selectWagons.getWagons();
                ImList<PlannedTrain> after = new ImList<>(
                        new PlannedTrain(engineType, wagonTypes));

                Move m = new ChangeProductionAtEngineShopMove(before, after, wi
                        .getIndex(), modelRoot.getPrincipal());
                modelRoot.doMove(m);
            }
            closeContent();
        }
    };

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
                            java.awt.event.ComponentEvent evt) {
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
     * <p>
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     * </p>
     */
    public void setup(ModelRootImpl mr, RenderersRoot vl) {
        this.modelRoot = mr;
        this.vl = vl;
        modelRoot.addListListener(this); // When a new train gets built, we
        // show the train info etc

        this.world = modelRoot.getWorld();

        if (world == null)
            throw new NullPointerException();

        if (vl == null)
            throw new NullPointerException();

        // Setup the various dialogue boxes.
        // setup the terrain info dialogue.
        terrainInfo.setup(world, vl);

        // setup the supply and demand at station dialogue.
        stationInfo.setup(modelRoot, vl, this.closeCurrentDialogue);
        modelRoot.addListListener(stationInfo);

        // setup the 'show controls' dialogue
        showControls.setup(this.modelRoot, vl, this.closeCurrentDialogue);

        about.setup(this.modelRoot, vl, this.closeCurrentDialogue);

        how2play.setup(this.modelRoot, vl, this.closeCurrentDialogue);

        javaProperties.setup(this.modelRoot, vl, this.closeCurrentDialogue);

        // Set up train orders dialogue
        // trainScheduleJPanel = new TrainScheduleJPanel();
        // trainScheduleJPanel.setup(w, vl);
        // moveChainFork.add(trainScheduleJPanel);
        // Set up select engine dialogue.
        selectEngine.setCancelButtonActionListener(this.closeCurrentDialogue);
        selectEngine.setup(modelRoot, vl, selectEngineAction);

        newspaper.setup(modelRoot, vl, closeCurrentDialogue);

        selectWagons.setup(modelRoot, vl, selectWagonsAction);

        trainDialogueJPanel.setup(modelRoot, vl, this.closeCurrentDialogue);
        modelRoot.addListListener(trainDialogueJPanel);
        trainDialogueJPanel
                .setTrainDetailsButtonActionListener(trainDetailsButtonActionListener);
        trainDialogueJPanel
                .setCancelButtonActionListener(this.closeCurrentDialogue);
    }

    public void showSaveGame() {
        SaveGameJPanel saveGameJPanel = new SaveGameJPanel();
        saveGameJPanel.setup(modelRoot, vl, this.closeCurrentDialogue);
        showContent(saveGameJPanel);
    }

    public void showSelectSavedGame2Load() {
        Message2Server refreshGames = new RefreshListOfGamesMessage2Server(2);
        modelRoot.sendCommand(refreshGames);
        LoadGameJPanel loadGameJPane = new LoadGameJPanel();
        loadGameJPane.setup(modelRoot, vl, this.closeCurrentDialogue);
        showContent(loadGameJPane);
    }

    public void showTrainOrders() {
        WorldIterator wi = new NonNullElements(KEY.TRAINS, world, modelRoot
                .getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Cannot"
                    + " show train orders since there are no" + " trains!");
        } else {
            trainDialogueJPanel.display(wi.getIndex());
            this.showContent(trainDialogueJPanel);
        }
    }

    public void showSelectEngine() {
        WorldIterator wi = new NonNullElements(KEY.STATIONS, world, modelRoot
                .getPrincipal());

        if (!wi.next()) {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Can't"
                    + " build train since there are no stations");
        } else {
            showContent(selectEngine);
        }
    }

    public void showGameControls() {
        showContent(this.showControls);
    }

    public void showIncomeStatement() {
        IncomeStatementHtmlJPanel bs = new IncomeStatementHtmlJPanel();
        bs.setup(this.modelRoot, vl, this.closeCurrentDialogue);
        this.showContent(bs);
    }

    public void showBalanceSheet() {
        BalanceSheetHtmlJPanel bs = new BalanceSheetHtmlJPanel();
        bs.setup(this.modelRoot, vl, this.closeCurrentDialogue);
        this.showContent(bs);
    }

    public void showReportBug() {
        CopyableTextJPanel ct = new CopyableTextJPanel();
        ct.setText(ReportBugTextGenerator.genText());
        showContent(ct);
    }

    public void showBrokerScreen() {
        // this is Creating a BrokerScreen Internal Frame in the Main Frame
        BrokerScreenHtmlJFrame brokerScreenHtmlJFrame = new BrokerScreenHtmlJFrame();
        brokerScreenHtmlJFrame.setup(this.modelRoot, vl,
                this.closeCurrentDialogue);
        brokerScreenHtmlJFrame.setFrameIcon(null);

        showContent(brokerScreenHtmlJFrame);
    }

    // Shows the Exit Dialog -- @author SonnyZ
    public void showExitDialog() {
        ConfirmExitJPanel bs = new ConfirmExitJPanel();
        bs.setup(this.modelRoot, vl, this.closeCurrentDialogue);
        this.showContent(bs);
    }

    public void showAbout() {
        showContent(this.about);
    }

    public void showHow2Play() {
        showContent(this.how2play);
    }

    public void showJavaProperties() {
        showContent(javaProperties);
    }

    public void showSelectWagons() {
        selectWagons.resetSelectedWagons();
        selectWagons.setEngineType(selectEngine.getEngineType());
        showContent(selectWagons);
    }

    public void showTerrainInfo(int terrainType) {
        this.terrainInfo.setTerrainType(terrainType);
        showContent(terrainInfo);
    }

    public void showTerrainInfo(int x, int y) {
        FreerailsTile tile = (FreerailsTile) world.getTile(x, y);
        int terrainType = tile.getTerrainTypeID();
        showTerrainInfo(terrainType);
    }

    public void showStationInfo(int stationNumber) {
        try {
            stationInfo.setStation(stationNumber);
            showContent(stationInfo);
        } catch (NoSuchElementException e) {
            logger.warn("Station " + stationNumber + " does not exist!");
        }
    }

    public void showTrainOrders(int trainId) {
        closeContent();

        if (trainId != -1) {
            trainDialogueJPanel.display(trainId);
            showContent(trainDialogueJPanel);
        }
    }

    public void showTrainList() {
        if (world.size(modelRoot.getPrincipal(), KEY.TRAINS) > 0) {
            final TrainListJPanel trainList = new TrainListJPanel();
            trainList.setup(modelRoot, vl, closeCurrentDialogue);
            trainList.setShowTrainDetailsActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    int id = trainList.getSelectedTrainID();
                    showTrainOrders(id);
                }
            });

            showContent(trainList);
        } else {
            modelRoot.setProperty(Property.QUICK_MESSAGE, "There are"
                    + " no trains to display!");
        }
    }

    public void showNetworthGraph() {

        final NetWorthGraphJPanel worthGraph = new NetWorthGraphJPanel();
        worthGraph.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(worthGraph);

    }

    public void showLeaderBoard() {

        LeaderBoardJPanel leaderBoardJPanel = new LeaderBoardJPanel();
        leaderBoardJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        showContent(leaderBoardJPanel);

    }

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

    public void setDefaultFocusOwner(Component defaultFocusOwner) {
        this.defaultFocusOwner = defaultFocusOwner;
    }

    public void showStationOrTerrainInfo(int x, int y) {
//        FreerailsTile tile = (FreerailsTile) world.getTile(x, y);
//
//        TrackRule trackRule = tile.getTrackPiece().getTrackRule();
//        FreerailsPrincipal principal = modelRoot.getPrincipal();
//        if (trackRule.isStation()
//                && tile.getTrackPiece().getOwnerID() == world.getID(principal)) {
//
//            for (int i = 0; i < world.size(principal, KEY.STATIONS); i++) {
//                StationModel station = (StationModel) world.get(principal,
//                        KEY.STATIONS, i);
//
//                if (null != station && station.x == x && station.y == y) {
//                    this.showStationInfo(i);
//
//                    return;
//                }
//            }
//
//            throw new IllegalStateException("Couldn't find station at " + x
//                    + ", " + y);
//        }
        int stationNumberAtLocation = StationHelper.getStationNumberAtLocation(world, modelRoot, x, y);
        if (stationNumberAtLocation > -1) {
            this.showStationInfo(stationNumberAtLocation);
        } else {
            this.showTerrainInfo(x, y);
        }


    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        // do nothing
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
        /*
         * Fix for: 910138 After building a train display train orders 910143
         * After building station show supply and demand
         */
        boolean rightPrincipal = principal
                .equals(this.modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            this.showTrainOrders(index);
        } else if (KEY.STATIONS == key && rightPrincipal) {
            this.showStationInfo(index);
        }
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
        // do nothing
    }
}