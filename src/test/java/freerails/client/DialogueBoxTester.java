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

package freerails.client;

import freerails.client.model.ServerControlModel;
import freerails.client.renderer.RendererRootImpl;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.client.receiver.TestMoveReceiver;
import freerails.util.WorldGenerator;
import freerails.util.ui.JFrameMinimumSizeEnforcer;
import freerails.util.ui.MyGlassPanel;
import freerails.client.renderer.RendererRoot;
import freerails.client.view.*;
import freerails.client.receiver.MoveChainFork;
import freerails.client.receiver.UntriedMoveReceiver;

import freerails.util.Vector2D;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.operations.station.StationDemand;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.world.World;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lets you test dialogue boxes without running the whole game.
 */
class DialogueBoxTester extends JFrame {

    private static final long serialVersionUID = 4050764909631780659L;
    private static final Player TEST_PLAYER = new Player(0, "test player");
    private final DialogueBoxController dialogueBoxController;
    private final ModelRootImpl modelRoot;
    private final Action closeCurrentDialogue = new AbstractAction("Close") {
        private static final long serialVersionUID = -7379893226410558610L;

        /**
         * Performs action performed.
         * @param e the e to use in the test.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            dialogueBoxController.closeContent();
        }
    };
    private final TrainDialoguePanel trainDialoguePanel = new TrainDialoguePanel();
    private RendererRoot vl;

    /**
     * Creates new form TestGlassPanelMethod.
     */
    private DialogueBoxTester() {
        World world = WorldGenerator.defaultWorld();

        UntriedMoveReceiver dummyReceiver = new TestMoveReceiver(world);

        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(new MoveChainFork());
        modelRoot.setMoveReceiver(dummyReceiver);

        world.addPlayer(TEST_PLAYER);
        try {
            vl = new RendererRootImpl(world);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modelRoot.setup(world, TEST_PLAYER);
        ActionRoot actionRoot = new ActionRoot(new ServerControlModel(modelRoot));
        actionRoot.setup(modelRoot, vl);
        dialogueBoxController = new DialogueBoxController(this, modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);
        dialogueBoxController.setDefaultFocusOwner(this);

        // Set up cargo bundle, for the purpose of this test code all the trains and stations can share the same one.
        CargoBatchBundle cargoBatchBundle = new CargoBatchBundle();
        cargoBatchBundle.setAmount(new CargoBatch(0, new Vector2D(10, 10), 8, 0), 80);
        cargoBatchBundle.setAmount(new CargoBatch(0, new Vector2D(10, 10), 9, 0), 60);
        cargoBatchBundle.setAmount(new CargoBatch(1, new Vector2D(10, 10), 9, 0), 140);
        cargoBatchBundle.setAmount(new CargoBatch(3, new Vector2D(10, 10), 9, 0), 180);
        cargoBatchBundle.setAmount(new CargoBatch(5, new Vector2D(10, 10), 9, 0), 10);

        Set<Integer> demandedCargoIds = world.getCargos().stream()
                .map(cargo -> cargo.getId())
                .collect(Collectors.toSet());
        Station bristol = new Station(0, new Vector2D(10, 10), "Bristol", cargoBatchBundle);
        StationDemand demand = new StationDemand(demandedCargoIds);
        bristol.setDemandForCargo(demand);
        world.addStation(TEST_PLAYER, bristol);

        world.addStation(TEST_PLAYER, new Station(1, new Vector2D(50, 100), "Bath", cargoBatchBundle));
        world.addStation(TEST_PLAYER, new Station(2, new Vector2D(40, 10), "Cardiff", cargoBatchBundle));
        world.addStation(TEST_PLAYER, new Station(3, new Vector2D(100, 10), "London", cargoBatchBundle));
        world.addStation(TEST_PLAYER, new Station(4, new Vector2D(90, 50), "Swansea", cargoBatchBundle));



        ScheduleState schedule = new ScheduleState();
        TrainOrder order = TrainOrder.specifiedConsist(0, Arrays.asList(0, 0, 0), false);
        TrainOrder order2 = TrainOrder.specifiedConsist(1, Arrays.asList(1, 2, 0, 0, 0), true);
        TrainOrder order3 = TrainOrder.keepConsist(2);
        schedule = schedule.setOrder(0, order);
        schedule = schedule.setOrder(1, order2);

        Train train = new Train(0,0);
        train.setConsist(Arrays.asList(0, 0));
        train.setCargoBatchBundle(cargoBatchBundle);
        train.setSchedule(schedule);
        world.addTrain(TEST_PLAYER, train);
        schedule = schedule.setOrder(2, order2);
        schedule = schedule.setOrder(3, order3);
        train = new Train(1,1);
        train.setConsist(Arrays.asList(1, 1));
        train.setCargoBatchBundle(cargoBatchBundle);
        train.setSchedule(schedule);
        world.addTrain(TEST_PLAYER, train);
        schedule = schedule.setOrder(4, order2);
        schedule = schedule.setOrderToGoto(3);
        schedule = schedule.addUrgentOrder(order);
        train = new Train(2,0);
        train.setConsist(Arrays.asList(1, 2, 0));
        train.setCargoBatchBundle(cargoBatchBundle);
        train.setSchedule(schedule);
        world.addTrain(TEST_PLAYER, train);

        final MyGlassPanel glassPanel = new MyGlassPanel();
        dialogueBoxController.setup(modelRoot, vl);
        JLabel label1 = new JLabel();
        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu show = new JMenu();
        JMenuItem showBrokerScreen = new JMenuItem();
        JMenuItem selectEngine = new JMenuItem();
        JMenuItem selectWagons = new JMenuItem();
        JMenuItem selectTrainOrders = new JMenuItem();
        JMenuItem showControls = new JMenuItem();
        JMenuItem showTerrainInfo = new JMenuItem();
        JMenuItem showStationInfo = new JMenuItem();
        JMenuItem showTrainList = new JMenuItem();
        JMenuItem throwException = new JMenuItem();
        JMenuItem showCargoWaitingAndDemand = new JMenuItem();
        JMenuItem showNetworthGraph = new JMenuItem();

        addKeyListener(new KeyAdapter() {
            /**
             * Performs key pressed.
             * @param e the e to use in the test.
             */
            @Override
            public void keyPressed(KeyEvent e) {
                formKeyPressed(e);
            }
        });
        addWindowListener(new WindowAdapter() {
            /**
             * Performs window closing.
             * @param e the e to use in the test.
             */
            @Override
            public void windowClosing(WindowEvent e) {
                exitForm(e);
            }
        });

        label1.setIcon(new ImageIcon(getClass().getResource(
                "/freerails/data/south_america.png")));
        label1.setText("Press Esc to close dialogue boxes");
        label1.setMinimumSize(new java.awt.Dimension(640, 480));
        label1.setPreferredSize(new java.awt.Dimension(640, 480));
        getContentPane().add(label1, java.awt.BorderLayout.CENTER);

        show.setText("Show");
        showBrokerScreen.setText("Broker Screen");
        showBrokerScreen.addActionListener(this::newspaperActionPerformed);

        show.add(showBrokerScreen);

        selectEngine.setText("Select Engine");
        selectEngine.addActionListener(this::selectEngineActionPerformed);

        show.add(selectEngine);

        selectWagons.setText("Select Wagons");
        selectWagons.addActionListener(this::selectWagonsActionPerformed);

        show.add(selectWagons);

        selectTrainOrders.setText("Train Orders");
        selectTrainOrders
                .addActionListener(this::selectTrainOrdersActionPerformed);

        show.add(selectTrainOrders);

        showControls.setText("Show game controls");
        showControls.addActionListener(this::showControlsActionPerformed);

        show.add(showControls);

        showTerrainInfo.setText("Show Terrain Info");
        showTerrainInfo.addActionListener(this::showTerrainInfoActionPerformed);

        show.add(showTerrainInfo);

        showStationInfo.setText("Show Station Info");
        showStationInfo.addActionListener(this::showStationInfoActionPerformed);

        show.add(showStationInfo);

        showTrainList.setText("Train List");
        showTrainList.addActionListener(this::showTrainListActionPerformed);

        show.add(showTrainList);

        showCargoWaitingAndDemand.setText("Cargo waiting & demand");
        showCargoWaitingAndDemand
                .addActionListener(this::showCargoWaitingAndDemandActionPerformed);

        show.add(showCargoWaitingAndDemand);

        throwException.setText("Throw Exception");
        throwException.addActionListener(e -> {
            throw new IllegalArgumentException();
        });

        showNetworthGraph.setText("Show net worth graph");
        showNetworthGraph
                .addActionListener(this::showNetworthGraphActionPerformed);

        show.add(showNetworthGraph);

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

        glassPanel.setSize(800, 600);
        addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));
        setSize(640, 480);
    }

    /**
     * Performs main.
     * @param args the args to use in the test.
     */
    public static void main(String[] args) {
        DialogueBoxTester test = new DialogueBoxTester();
        test.setVisible(true);
    }


    /**
     * Performs main.
     * @param args the args to use in the test.
     */
    private void showNetworthGraphActionPerformed(ActionEvent evt) {
        dialogueBoxController.showNetworthGraph();
    }

    /**
     * Performs form key pressed.
     * @param evt the evt to use in the test.
     */
    private void formKeyPressed(KeyEvent evt) {
        if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
            dialogueBoxController.closeContent();
        }
    }

    /**
     * Performs form key pressed.
     * @param evt the evt to use in the test.
     */
    private void showCargoWaitingAndDemandActionPerformed(
            ActionEvent evt) {
        
        CargoWaitingAndDemandedPanel panel = new CargoWaitingAndDemandedPanel();
        panel.setup(modelRoot, vl, closeCurrentDialogue);
        int newStationID = 0;
        panel.display(newStationID);
        dialogueBoxController.showContent(panel);
    }

    /**
     * Performs show train list action performed.
     * @param evt the evt to use in the test.
     */
    private void showTrainListActionPerformed(ActionEvent evt) {
        
        dialogueBoxController.showTrainList();
    }

    /**
     * Performs show station info action performed.
     * @param evt the evt to use in the test.
     */
    private void showStationInfoActionPerformed(ActionEvent evt) {
        
        int stationNumber = 0;
        dialogueBoxController.showStationInfo(stationNumber);
    }

    /**
     * Performs show station info action performed.
     * @param evt the evt to use in the test.
     */
    private void showTerrainInfoActionPerformed(ActionEvent evt) {
        
        int terrainType = 0;
        dialogueBoxController.showTerrainInfo(terrainType);
    }

    /**
     * Performs show terrain info action performed.
     * @param evt the evt to use in the test.
     */
    private void showControlsActionPerformed(ActionEvent evt) {
        
        dialogueBoxController.showGameControls();
    }

    /**
     * Performs select train orders action performed.
     * @param evt the evt to use in the test.
     */
    private void selectTrainOrdersActionPerformed(ActionEvent evt) {
        
        trainDialoguePanel.setup(modelRoot, vl, closeCurrentDialogue);
        trainDialoguePanel.display(0);
        dialogueBoxController.showContent(trainDialoguePanel);
    }

    /**
     * Performs select train orders action performed.
     * @param evt the evt to use in the test.
     */
    private void selectWagonsActionPerformed(ActionEvent evt) {
        
        dialogueBoxController.showSelectWagons();
    }

    /**
     * Performs select engine action performed.
     * @param evt the evt to use in the test.
     */
    private void selectEngineActionPerformed(ActionEvent evt) {
        
        dialogueBoxController.showSelectEngine();
    }

    /**
     * Performs newspaper action performed.
     * @param evt the evt to use in the test.
     */
    private void newspaperActionPerformed(ActionEvent evt) {
        
        dialogueBoxController.showBrokerScreen();
        // dialogueBoxController.showNewspaper("New headline!");
    }

    /**
     * Exit the Application.
     */
    private void exitForm(WindowEvent evt) {
        System.exit(0);
    }

}
