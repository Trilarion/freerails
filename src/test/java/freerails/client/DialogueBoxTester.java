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

package freerails.client;

import experimental.SimpleMoveReciever;
import freerails.client.common.ModelRootImpl;
import freerails.client.common.MyGlassPanel;
import freerails.client.renderer.RendererRoot;
import freerails.client.view.*;
import freerails.network.MoveChainFork;
import freerails.network.UntriedMoveReceiver;
import freerails.server.TileSetFactory;
import freerails.server.TileSetFactoryImpl;
import freerails.util.ImmutableList;
import freerails.world.*;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBatchBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.station.StationDemand;
import freerails.world.station.Station;
import freerails.world.train.MutableSchedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;
import freerails.world.train.WagonAndEngineTypesFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Lets you test dialogue boxes without running the whole game.
 */
@SuppressWarnings("unused")
public class DialogueBoxTester extends javax.swing.JFrame {

    private static final long serialVersionUID = 4050764909631780659L;
    private static final Player TEST_PLAYER = new Player("test player", 0);
    private static final FreerailsPrincipal TEST_PRINCIPAL = TEST_PLAYER.getPrincipal();
    private final DialogueBoxController dialogueBoxController;
    private final ModelRootImpl modelRoot;
    private final Action closeCurrentDialogue = new AbstractAction("Close") {

        private static final long serialVersionUID = -7379893226410558610L;

        public void actionPerformed(ActionEvent e) {
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
    javax.swing.JMenuItem throwException;
    private RendererRoot vl;

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
        WagonAndEngineTypesFactory.addTypesToWorld(w);
        w.addPlayer(TEST_PLAYER);
        try {
            vl = new RendererRootImpl(w,
                    ProgressMonitorModel.EMPTY_PROGRESSMONITOR);
        } catch (IOException e) {
        }
        modelRoot.setup(w, TEST_PLAYER.getPrincipal());
        ActionRoot actionRoot = new ActionRoot(modelRoot);
        actionRoot.setup(modelRoot, vl);
        dialogueBoxController = new DialogueBoxController(this, modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);
        dialogueBoxController.setDefaultFocusOwner(this);

        int numberOfCargoTypes = w.size(SKEY.CARGO_TYPES);
        Station bristol = new Station(10, 10, "Bristol",
                numberOfCargoTypes, 0);
        boolean[] demandArray = new boolean[numberOfCargoTypes];

        // Make the stations demand all cargo..
        for (int i = 0; i < demandArray.length; i++) {
            demandArray[i] = true;
        }

        StationDemand demand = new StationDemand(demandArray);
        bristol = new Station(bristol, demand);
        w.add(TEST_PRINCIPAL, KEY.STATIONS, bristol);
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new Station(50, 100, "Bath",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new Station(40, 10, "Cardiff",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new Station(100, 10, "London",
                numberOfCargoTypes, 0));
        w.add(TEST_PRINCIPAL, KEY.STATIONS, new Station(90, 50, "Swansea",
                numberOfCargoTypes, 0));
        // Set up cargo bundle, for the purpose of this test code all the trains
        // can share the
        // same one.
        MutableCargoBatchBundle cb = new MutableCargoBatchBundle();
        cb.setAmount(new CargoBatch(0, 10, 10, 8, 0), 80);
        cb.setAmount(new CargoBatch(0, 10, 10, 9, 0), 60);
        cb.setAmount(new CargoBatch(1, 10, 10, 9, 0), 140);
        cb.setAmount(new CargoBatch(3, 10, 10, 9, 0), 180);
        cb.setAmount(new CargoBatch(5, 10, 10, 9, 0), 10);
        w.add(TEST_PRINCIPAL, KEY.CARGO_BUNDLES, cb.toImmutableCargoBundle());

        MutableSchedule schedule = new MutableSchedule();
        TrainOrdersModel order = new TrainOrdersModel(0, new ImmutableList<>(0, 0, 0),
                false, false);
        TrainOrdersModel order2 = new TrainOrdersModel(1, new ImmutableList<>(1, 2, 0,
                0, 0), true, false);
        TrainOrdersModel order3 = new TrainOrdersModel(2, null, true, false);
        schedule.setOrder(0, order);
        schedule.setOrder(1, order2);

        int scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(0, new ImmutableList<>(0, 0),
                scheduleID));
        schedule.setOrder(2, order2);
        schedule.setOrder(3, order3);
        scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(1, new ImmutableList<>(1, 1),
                scheduleID));
        schedule.setOrder(4, order2);
        schedule.setOrderToGoto(3);
        schedule.setPriorityOrders(order);
        scheduleID = w.add(TEST_PRINCIPAL, KEY.TRAIN_SCHEDULES, schedule
                .toImmutableSchedule());
        w.add(TEST_PRINCIPAL, KEY.TRAINS, new TrainModel(0,
                new ImmutableList<>(1, 2, 0), scheduleID));

        final MyGlassPanel glassPanel = new MyGlassPanel();
        dialogueBoxController.setup(modelRoot, vl);
        initComponents();

        glassPanel.setSize(800, 600);
        addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));
        setSize(640, 480);

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
    private void initComponents() {
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
        throwException = new javax.swing.JMenuItem();
        showCargoWaitingAndDemand = new javax.swing.JMenuItem();
        showJavaSystemProperties = new javax.swing.JMenuItem();
        showNetworthGraph = new javax.swing.JMenuItem();

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                formKeyPressed(e);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitForm(e);
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
        showBrokerScreen.addActionListener(e -> newspaperActionPerformed(e));

        show.add(showBrokerScreen);

        selectEngine.setText("Select Engine");
        selectEngine.addActionListener(e -> selectEngineActionPerformed(e));

        show.add(selectEngine);

        selectWagons.setText("Select Wagons");
        selectWagons.addActionListener(e -> selectWagonsActionPerformed(e));

        show.add(selectWagons);

        selectTrainOrders.setText("Train Orders");
        selectTrainOrders
                .addActionListener(e -> selectTrainOrdersActionPerformed(e));

        show.add(selectTrainOrders);

        showControls.setText("Show game controls");
        showControls.addActionListener(e -> showControlsActionPerformed(e));

        show.add(showControls);

        showTerrainInfo.setText("Show Terrain Info");
        showTerrainInfo.addActionListener(e -> showTerrainInfoActionPerformed(e));

        show.add(showTerrainInfo);

        showStationInfo.setText("Show Station Info");
        showStationInfo.addActionListener(e -> showStationInfoActionPerformed(e));

        show.add(showStationInfo);

        showTrainList.setText("Train List");
        showTrainList.addActionListener(e -> showTrainListActionPerformed(e));

        show.add(showTrainList);

        showCargoWaitingAndDemand.setText("Cargo waiting & demand");
        showCargoWaitingAndDemand
                .addActionListener(e -> showCargoWaitingAndDemandActionPerformed(e));

        show.add(showCargoWaitingAndDemand);

        showJavaSystemProperties.setText("Java System Properties");
        showJavaSystemProperties
                .addActionListener(e -> showJavaSystemPropertiesActionPerformed(e));

        throwException.setText("Throw Exception");
        throwException.addActionListener(e -> {
            throw new IllegalArgumentException();
        });

        show.add(showJavaSystemProperties);

        showNetworthGraph.setText("Show net worth graph");
        showNetworthGraph
                .addActionListener(e -> showNetworthGraphActionPerformed(e));

        show.add(showNetworthGraph);

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

    }

    private void showNetworthGraphActionPerformed(java.awt.event.ActionEvent evt) {
        dialogueBoxController.showNetworthGraph();
    }

    private void showJavaSystemPropertiesActionPerformed(
            java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        String s = ShowJavaProperties.getPropertiesHtmlString();
        HtmlJPanel htmlPanel = new HtmlJPanel(s);
        htmlPanel.setup(modelRoot, vl, closeCurrentDialogue);
        dialogueBoxController.showContent(htmlPanel);
    }

    private void formKeyPressed(java.awt.event.KeyEvent evt) {
        if (java.awt.event.KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
            dialogueBoxController.closeContent();
        }
    }

    private void showCargoWaitingAndDemandActionPerformed(
            java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        CargoWaitingAndDemandedJPanel panel = new CargoWaitingAndDemandedJPanel();
        panel.setup(modelRoot, vl, closeCurrentDialogue);
        int newStationID = 0;
        panel.display(newStationID);
        dialogueBoxController.showContent(panel);
    }

    private void showTrainListActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        dialogueBoxController.showTrainList();
    }

    private void showStationInfoActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        int stationNumber = 0;
        dialogueBoxController.showStationInfo(stationNumber);
    }

    private void showTerrainInfoActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        int terrainType = 0;
        dialogueBoxController.showTerrainInfo(terrainType);
    }

    private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        dialogueBoxController.showGameControls();
    }

    private void selectTrainOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        trainDialogueJPanel.setup(modelRoot, vl, closeCurrentDialogue);
        trainDialogueJPanel.display(0);
        dialogueBoxController.showContent(trainDialogueJPanel);
    }

    private void selectWagonsActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        dialogueBoxController.showSelectWagons();
    }

    private void selectEngineActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        dialogueBoxController.showSelectEngine();
    }

    private void newspaperActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        dialogueBoxController.showBrokerScreen();
        // dialogueBoxController.showNewspaper("New headline!");
    }

    /**
     * Exit the Application.
     */
    private void exitForm(java.awt.event.WindowEvent evt) {
        System.exit(0);
    }
    // End of variables declaration//GEN-END:variables

}
