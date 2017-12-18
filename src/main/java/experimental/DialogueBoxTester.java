package experimental;

import freerails.client.common.ModelRootImpl;
import freerails.client.common.MyGlassPanel;
import freerails.client.renderer.RenderersRoot;
import freerails.client.top.RenderersRootImpl;
import freerails.client.view.*;
import freerails.controller.JFrameMinimumSizeEnforcer;
import freerails.network.MoveChainFork;
import freerails.network.UntriedMoveReceiver;
import freerails.server.TileSetFactoryImpl;
import freerails.server.common.TileSetFactory;
import freerails.util.FreerailsProgressMonitor;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.common.ImInts;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.station.Demand4Cargo;
import freerails.world.station.StationModel;
import freerails.world.top.*;
import freerails.world.train.MutableSchedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * This class lets you test dialogue boxes without running the whole game.
 *
 * @author lindsal8
 */
public class DialogueBoxTester extends javax.swing.JFrame {

    private static final long serialVersionUID = 4050764909631780659L;

    private static final Player TEST_PLAYER = new Player("test player", 0);

    private static final FreerailsPrincipal TEST_PRINCIPAL = TEST_PLAYER
            .getPrincipal();

    private final DialogueBoxController dialogueBoxController;
    private final ModelRootImpl modelRoot;
    private final Action closeCurrentDialogue = new AbstractAction("Close") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent arg0) {
            dialogueBoxController.closeContent();
        }
    };
    private final TrainDialogueJPanel trainDialogueJPanel = new TrainDialogueJPanel();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JLabel jLabel1;
    javax.swing.JMenuBar jMenuBar1;
    javax.swing.JMenuItem showBrokerScreen;
    javax.swing.JMenuItem selectEngine;
    javax.swing.JMenuItem selectTrainOrders;
    javax.swing.JMenuItem selectWagons;
    javax.swing.JMenu show;
    javax.swing.JMenuItem showCargoWaitingAndDemand;
    javax.swing.JMenuItem showControls;
    javax.swing.JMenuItem showJavaSystemProperties;
    javax.swing.JMenuItem showNetworthGraph;
    javax.swing.JMenuItem showStationInfo;
    javax.swing.JMenuItem showTerrainInfo;
    javax.swing.JMenuItem showTrainList;
    javax.swing.JMenuItem showReportBug;
    javax.swing.JMenuItem throwException;
    private RenderersRoot vl;

    /**
     * Creates new form TestGlassPanelMethod.
     */
    private DialogueBoxTester() {

        World w = new WorldImpl(200, 200);

        UntriedMoveReceiver dummyReceiver = new SimpleMoveReciever(w);

        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(new MoveChainFork());
        modelRoot.setMoveReceiver(dummyReceiver);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        TileSetFactory tileFactory = new TileSetFactoryImpl();
        tileFactory.addTerrainTileTypesList(w);
        wetf.addTypesToWorld(w);
        w.addPlayer(TEST_PLAYER);
        try {
            vl = new RenderersRootImpl(w,
                    FreerailsProgressMonitor.NULL_INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modelRoot.setup(w, TEST_PLAYER.getPrincipal());
        ActionRoot actionRoot = new ActionRoot(modelRoot);
        actionRoot.setup(modelRoot, vl);
        dialogueBoxController = new DialogueBoxController(this, modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);
        dialogueBoxController.setDefaultFocusOwner(this);

        int numberOfCargoTypes = w.size(SKEY.CARGO_TYPES);
        StationModel bristol = new StationModel(10, 10, "Bristol",
                numberOfCargoTypes, 0);
        boolean[] demandArray = new boolean[numberOfCargoTypes];

        // Make the stations demand all cargo..
        for (int i = 0; i < demandArray.length; i++) {
            demandArray[i] = true;
        }

        Demand4Cargo demand = new Demand4Cargo(demandArray);
        bristol = new StationModel(bristol, demand);
        w.add(TEST_PRINCIPAL, KEY.STATIONS, bristol);
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new StationModel(50, 100, "Bath",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new StationModel(40, 10, "Cardiff",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new StationModel(100, 10, "London",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new StationModel(90, 50, "Swansea",
                numberOfCargoTypes, 0));
        // Set up cargo bundle, for the purpose of this test code all the trains
        // can share the
        // same one.
        MutableCargoBundle cb = new MutableCargoBundle();
        cb.setAmount(new CargoBatch(0, 10, 10, 8, 0), 80);
        cb.setAmount(new CargoBatch(0, 10, 10, 9, 0), 60);
        cb.setAmount(new CargoBatch(1, 10, 10, 9, 0), 140);
        cb.setAmount(new CargoBatch(3, 10, 10, 9, 0), 180);
        cb.setAmount(new CargoBatch(5, 10, 10, 9, 0), 10);
        w.add(TEST_PRINCIPAL, KEY.CARGO_BUNDLES, cb.toImmutableCargoBundle());

        MutableSchedule schedule = new MutableSchedule();
        TrainOrdersModel order = new TrainOrdersModel(0, new ImInts(0, 0, 0),
                false, false);
        TrainOrdersModel order2 = new TrainOrdersModel(1, new ImInts(1, 2, 0,
                0, 0), true, false);
        TrainOrdersModel order3 = new TrainOrdersModel(2, null, true, false);
        schedule.setOrder(0, order);
        schedule.setOrder(1, order2);

        int scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(0, new ImInts(0, 0),
                scheduleID));
        schedule.setOrder(2, order2);
        schedule.setOrder(3, order3);
        scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(1, new ImInts(1, 1),
                scheduleID));
        schedule.setOrder(4, order2);
        schedule.setOrderToGoto(3);
        schedule.setPriorityOrders(order);
        scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(0,
                new ImInts(1, 2, 0), scheduleID));

        final MyGlassPanel glassPanel = new MyGlassPanel();
        dialogueBoxController.setup(modelRoot, vl);
        initComponents();

        glassPanel.setSize(800, 600);
        this.addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));
        this.setSize(640, 480);

    }

    public static void main(String args[]) {
        DialogueBoxTester test = new DialogueBoxTester();
        test.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    private void initComponents() {// GEN-BEGIN:initComponents
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        show = new javax.swing.JMenu();
        showBrokerScreen = new javax.swing.JMenuItem();
        selectEngine = new javax.swing.JMenuItem();
        selectWagons = new javax.swing.JMenuItem();
        selectTrainOrders = new javax.swing.JMenuItem();
        showControls = new javax.swing.JMenuItem();
        showTerrainInfo = new javax.swing.JMenuItem();
        showStationInfo = new javax.swing.JMenuItem();
        showTrainList = new javax.swing.JMenuItem();
        showReportBug = new javax.swing.JMenuItem();
        throwException = new javax.swing.JMenuItem();
        showCargoWaitingAndDemand = new javax.swing.JMenuItem();
        showJavaSystemProperties = new javax.swing.JMenuItem();
        showNetworthGraph = new javax.swing.JMenuItem();

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource(
                "/freerails/data/south_america.png")));
        jLabel1.setText("Press Esc to close dialogue boxes");
        jLabel1.setMinimumSize(new java.awt.Dimension(640, 480));
        jLabel1.setPreferredSize(new java.awt.Dimension(640, 480));
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        show.setText("Show");
        showBrokerScreen.setText("Broker Screen");
        showBrokerScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newspaperActionPerformed(evt);
            }
        });

        show.add(showBrokerScreen);

        selectEngine.setText("Select Engine");
        selectEngine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectEngineActionPerformed(evt);
            }
        });

        show.add(selectEngine);

        selectWagons.setText("Select Wagons");
        selectWagons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectWagonsActionPerformed(evt);
            }
        });

        show.add(selectWagons);

        selectTrainOrders.setText("Train Orders");
        selectTrainOrders
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectTrainOrdersActionPerformed(evt);
                    }
                });

        show.add(selectTrainOrders);

        showControls.setText("Show game controls");
        showControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showControlsActionPerformed(evt);
            }
        });

        show.add(showControls);

        showTerrainInfo.setText("Show Terrain Info");
        showTerrainInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTerrainInfoActionPerformed(evt);
            }
        });

        show.add(showTerrainInfo);

        showStationInfo.setText("Show Station Info");
        showStationInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showStationInfoActionPerformed(evt);
            }
        });

        show.add(showStationInfo);

        showTrainList.setText("Train List");
        showTrainList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrainListActionPerformed(evt);
            }
        });

        show.add(showTrainList);

        showCargoWaitingAndDemand.setText("Cargo waiting & demand");
        showCargoWaitingAndDemand
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        showCargoWaitingAndDemandActionPerformed(evt);
                    }
                });

        show.add(showCargoWaitingAndDemand);

        showJavaSystemProperties.setText("Java System Properties");
        showJavaSystemProperties
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        showJavaSystemPropertiesActionPerformed(evt);
                    }
                });

        throwException.setText("Throw Exception");
        throwException.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                throw new IllegalArgumentException();
            }
        });

        show.add(showJavaSystemProperties);

        showNetworthGraph.setText("Show networth graph");
        showNetworthGraph
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        showNetworthGraphActionPerformed(evt);
                    }
                });

        show.add(showNetworthGraph);

        showReportBug.setText("Report Bug");
        showReportBug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogueBoxController.showReportBug();
            }

        });

        show.add(showReportBug);

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

    }// GEN-END:initComponents

    private void showNetworthGraphActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showNetworthGraphActionPerformed
        dialogueBoxController.showNetworthGraph();
    }// GEN-LAST:event_showNetworthGraphActionPerformed

    private void showJavaSystemPropertiesActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showJavaSystemPropertiesActionPerformed
        // Add your handling code here:
        String s = ShowJavaProperties.getPropertiesHtmlString();
        HtmlJPanel htmlPanel = new HtmlJPanel(s);
        htmlPanel.setup(modelRoot, vl, closeCurrentDialogue);
        dialogueBoxController.showContent(htmlPanel);
    }// GEN-LAST:event_showJavaSystemPropertiesActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_formKeyPressed
        if (java.awt.event.KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
            dialogueBoxController.closeContent();
        }
    }// GEN-LAST:event_formKeyPressed

    private void showCargoWaitingAndDemandActionPerformed(
            java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showCargoWaitingAndDemandActionPerformed
        // Add your handling code here:
        CargoWaitingAndDemandedJPanel panel = new CargoWaitingAndDemandedJPanel();
        panel.setup(modelRoot, vl, closeCurrentDialogue);
        int newStationID = 0;
        panel.display(newStationID);
        dialogueBoxController.showContent(panel);
    }// GEN-LAST:event_showCargoWaitingAndDemandActionPerformed

    private void showTrainListActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showTrainListActionPerformed
        // Add your handling code here:
        dialogueBoxController.showTrainList();
    }// GEN-LAST:event_showTrainListActionPerformed

    private void showStationInfoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showStationInfoActionPerformed
        // Add your handling code here:
        int stationNumber = 0;
        dialogueBoxController.showStationInfo(stationNumber);
    }// GEN-LAST:event_showStationInfoActionPerformed

    private void showTerrainInfoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showTerrainInfoActionPerformed
        // Add your handling code here:
        int terrainType = 0;
        dialogueBoxController.showTerrainInfo(terrainType);
    }// GEN-LAST:event_showTerrainInfoActionPerformed

    private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_showControlsActionPerformed
        // Add your handling code here:
        dialogueBoxController.showGameControls();
    }// GEN-LAST:event_showControlsActionPerformed

    private void selectTrainOrdersActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectTrainOrdersActionPerformed
        // Add your handling code here:
        trainDialogueJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        trainDialogueJPanel.display(0);
        dialogueBoxController.showContent(trainDialogueJPanel);
    }// GEN-LAST:event_selectTrainOrdersActionPerformed

    private void selectWagonsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectWagonsActionPerformed
        // Add your handling code here:
        dialogueBoxController.showSelectWagons();
    }// GEN-LAST:event_selectWagonsActionPerformed

    private void selectEngineActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectEngineActionPerformed
        // Add your handling code here:
        dialogueBoxController.showSelectEngine();
    }// GEN-LAST:event_selectEngineActionPerformed

    private void newspaperActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_newspaperActionPerformed
        // Add your handling code here:
        dialogueBoxController.showBrokerScreen();
        // dialogueBoxController.showNewspaper("New headline!");
    }// GEN-LAST:event_newspaperActionPerformed

    /**
     * Exit the Application.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
        System.exit(0);
    }// GEN-LAST:event_exitForm
    // End of variables declaration//GEN-END:variables

}
