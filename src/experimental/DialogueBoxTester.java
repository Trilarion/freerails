package experimental;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import jfreerails.client.common.JFrameMinimumSizeEnforcer;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.top.ViewListsImpl;
import jfreerails.client.view.ActionRoot;
import jfreerails.client.view.CargoWaitingAndDemandedJPanel;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.HtmlJPanel;
import jfreerails.client.view.ShowJavaProperties;
import jfreerails.client.view.TrainDialogueJPanel;
import jfreerails.move.ListMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.network.MoveChainFork;
import jfreerails.network.UntriedMoveReceiver;
import jfreerails.server.NewTileSetFactoryImpl;
import jfreerails.server.common.TileSetFactory;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.MutableCargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
/**
 * This class lets you test dialogue boxes without running the whole game.
 * @author  lindsal8
 *
 */
public class DialogueBoxTester extends javax.swing.JFrame {
    
    private static final Player TEST_PLAYER = new Player("test player",
    (new Player("test player")).getPublicKey(), 0);
    private static final FreerailsPrincipal TEST_PRINCIPAL = TEST_PLAYER.getPrincipal();
    
    
    private final DialogueBoxController dialogueBoxController;
    
    private World w;
    
    private ViewLists vl;
    
    private ModelRootImpl modelRoot;
    
    private ActionListener closeCurrentDialogue = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            dialogueBoxController.closeContent();
        }
    };
    
    private UntriedMoveReceiver dummyReceiver = new SimpleMoveReciever(w);
    
    private TrainDialogueJPanel trainDialogueJPanel = new TrainDialogueJPanel();
    
    /** Creates new form TestGlassPanelMethod. */
    private DialogueBoxTester() {
        
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(new MoveChainFork());
        modelRoot.setMoveReceiver(this.dummyReceiver);
        w = new WorldImpl(200, 200);
        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        TileSetFactory tileFactory = new NewTileSetFactoryImpl();
        tileFactory.addTerrainTileTypesList(w);
        wetf.addTypesToWorld(w);
        w.addPlayer(TEST_PLAYER);
        try {
            vl = new ViewListsImpl(w, FreerailsProgressMonitor.NULL_INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modelRoot.setup(w, TEST_PLAYER.getPrincipal());
        ActionRoot actionRoot = new ActionRoot();
        actionRoot.setup(modelRoot, vl);
        dialogueBoxController = new DialogueBoxController(this, modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);
        dialogueBoxController.setDefaultFocusOwner(this);
        
        int numberOfCargoTypes = w.size(SKEY.CARGO_TYPES);
        StationModel bristol = new StationModel(10, 10, "Bristol", numberOfCargoTypes, 0);
        boolean [] demandArray = new boolean [numberOfCargoTypes];
        
        //Make the stations demand all cargo..
        for(int i = 0 ; i < demandArray.length; i++){
            demandArray[i]=true;
        }
        
        DemandAtStation demand = new DemandAtStation(demandArray);
        bristol = new StationModel(bristol, demand);
        w.add(
        KEY.STATIONS,
        bristol, TEST_PRINCIPAL);
        w.add(
        KEY.STATIONS,
        new StationModel(50, 100, "Bath", numberOfCargoTypes, 0), TEST_PRINCIPAL);
        w.add(
        KEY.STATIONS,
        new StationModel(40, 10, "Cardiff", numberOfCargoTypes, 0), TEST_PRINCIPAL);
        w.add(
        KEY.STATIONS,
        new StationModel(100, 10, "London", numberOfCargoTypes, 0), TEST_PRINCIPAL);
        w.add(
        KEY.STATIONS,
        new StationModel(90, 50, "Swansea", numberOfCargoTypes, 0), TEST_PRINCIPAL);
        //Set up cargo bundle, for the purpose of this test code all the trains can share the
        //same one.
        MutableCargoBundle cb = new MutableCargoBundle();
        cb.setAmount(new CargoBatch(0, 10, 10, 8, 0), 80);
        cb.setAmount(new CargoBatch(0, 10, 10, 9, 0), 60);
        cb.setAmount(new CargoBatch(1, 10, 10, 9, 0), 140);
        cb.setAmount(new CargoBatch(3, 10, 10, 9, 0), 180);
        cb.setAmount(new CargoBatch(5, 10, 10, 9, 0), 10);
        w.add(KEY.CARGO_BUNDLES, cb.toImmutableCargoBundle(), TEST_PRINCIPAL);
        
        MutableSchedule schedule = new MutableSchedule();
        TrainOrdersModel order =
        new TrainOrdersModel(0, new int[] { 0, 0, 0 }, false, false);
        TrainOrdersModel order2 =
        new TrainOrdersModel(1, new int[] { 1, 2, 0, 0,0 }, true, false);
        TrainOrdersModel order3 =
        new TrainOrdersModel(2, null, true, false);
        schedule.setOrder(0, order);
        schedule.setOrder(1, order2);
        
        int scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 0, 0 },  scheduleID), TEST_PRINCIPAL);
        schedule.setOrder(2, order2);
        schedule.setOrder(3, order3);
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(1, new int[] { 1, 1 },  scheduleID), TEST_PRINCIPAL);
        schedule.setOrder(4, order2);
        schedule.setOrderToGoto(3);
        schedule.setPriorityOrders(order);
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 1, 2, 0 },  scheduleID), TEST_PRINCIPAL);
        
        
        final MyGlassPanel glassPanel = new MyGlassPanel();
        dialogueBoxController.setup(modelRoot, vl);
        initComponents();
        
        glassPanel.setSize(800, 600);
        this.addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));
        this.setSize(640, 480);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        show = new javax.swing.JMenu();
        newspaper = new javax.swing.JMenuItem();
        selectEngine = new javax.swing.JMenuItem();
        selectWagons = new javax.swing.JMenuItem();
        selectTrainOrders = new javax.swing.JMenuItem();
        showControls = new javax.swing.JMenuItem();
        showTerrainInfo = new javax.swing.JMenuItem();
        showStationInfo = new javax.swing.JMenuItem();
        showTrainList = new javax.swing.JMenuItem();
        showCargoWaitingAndDemand = new javax.swing.JMenuItem();
        showJavaSystemProperties = new javax.swing.JMenuItem();
        showNetworthGraph = new javax.swing.JMenuItem();

        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/data/south_america.png")));
        jLabel1.setText("Press Esc to close dialogue boxes");
        jLabel1.setMinimumSize(new java.awt.Dimension(640, 480));
        jLabel1.setPreferredSize(new java.awt.Dimension(640, 480));
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        show.setText("Show");
        newspaper.setText("Newspaper");
        newspaper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newspaperActionPerformed(evt);
            }
        });

        show.add(newspaper);

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
        selectTrainOrders.addActionListener(new java.awt.event.ActionListener() {
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
        showCargoWaitingAndDemand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCargoWaitingAndDemandActionPerformed(evt);
            }
        });

        show.add(showCargoWaitingAndDemand);

        showJavaSystemProperties.setText("Java System Properties");
        showJavaSystemProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJavaSystemPropertiesActionPerformed(evt);
            }
        });

        show.add(showJavaSystemProperties);

        showNetworthGraph.setText("Show networth graph");
        showNetworthGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNetworthGraphActionPerformed(evt);
            }
        });

        show.add(showNetworthGraph);

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents
    
    private void showNetworthGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNetworthGraphActionPerformed
        dialogueBoxController.showNetworthGraph();
    }//GEN-LAST:event_showNetworthGraphActionPerformed
    
    private void showJavaSystemPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavaSystemPropertiesActionPerformed
        // Add your handling code here:
        String s = ShowJavaProperties.getPropertiesHtmlString();
        HtmlJPanel htmlPanel = new HtmlJPanel(s);
        htmlPanel.setup(modelRoot, vl, closeCurrentDialogue);
        dialogueBoxController.showContent(htmlPanel);
    }//GEN-LAST:event_showJavaSystemPropertiesActionPerformed
    
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if(java.awt.event.KeyEvent.VK_ESCAPE == evt.getKeyCode()){
            dialogueBoxController.closeContent();
        }
    }//GEN-LAST:event_formKeyPressed
    
    private void showCargoWaitingAndDemandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCargoWaitingAndDemandActionPerformed
        // Add your handling code here:
        CargoWaitingAndDemandedJPanel panel = new CargoWaitingAndDemandedJPanel();
        panel.setup(modelRoot, vl, closeCurrentDialogue);
        int newStationID = 0;
        panel.display(newStationID);
        dialogueBoxController.showContent(panel);
    }//GEN-LAST:event_showCargoWaitingAndDemandActionPerformed
    
    private void showTrainListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrainListActionPerformed
        // Add your handling code here:
        dialogueBoxController.showTrainList();
    }//GEN-LAST:event_showTrainListActionPerformed
    
    
    
	private void showStationInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStationInfoActionPerformed
            // Add your handling code here:
            int stationNumber = 0;
            dialogueBoxController.showStationInfo(stationNumber);
	}//GEN-LAST:event_showStationInfoActionPerformed
        
	private void showTerrainInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTerrainInfoActionPerformed
            // Add your handling code here:
            int terrainType = 0;
            dialogueBoxController.showTerrainInfo(terrainType);
	}//GEN-LAST:event_showTerrainInfoActionPerformed
        
	private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showControlsActionPerformed
            // Add your handling code here:
            dialogueBoxController.showGameControls();
	}//GEN-LAST:event_showControlsActionPerformed
        
	private void selectTrainOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTrainOrdersActionPerformed
            // Add your handling code here:
            trainDialogueJPanel.setup(modelRoot, vl, closeCurrentDialogue);
            trainDialogueJPanel.display(0);
            dialogueBoxController.showContent(trainDialogueJPanel);
	}//GEN-LAST:event_selectTrainOrdersActionPerformed
        
	private void selectWagonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectWagonsActionPerformed
            // Add your handling code here:
            dialogueBoxController.showSelectWagons();
	}//GEN-LAST:event_selectWagonsActionPerformed
        
	private void selectEngineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectEngineActionPerformed
            // Add your handling code here:
            dialogueBoxController.showSelectEngine();
	}//GEN-LAST:event_selectEngineActionPerformed
        
	private void newspaperActionPerformed(java.awt.event.ActionEvent evt) {	//GEN-FIRST:event_newspaperActionPerformed
            // Add your handling code here:
            dialogueBoxController.showNewspaper("New headline!");
	}//GEN-LAST:event_newspaperActionPerformed
        
        /** Exit the Application. */
	private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
            System.exit(0);
	}//GEN-LAST:event_exitForm
        
        
        public static void main(String args[]) {
            DialogueBoxTester test = new DialogueBoxTester();
            test.setVisible(true);
        }
        
        
        public void processMove(Move m) {
            MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);
            if(!ms.ok){
                throw new IllegalArgumentException(ms.message);
            }
            if(m instanceof ListMove){
                ListMove lm = (ListMove)m;
                trainDialogueJPanel.listUpdated(lm.getKey(), lm.getIndex(), lm.getPrincipal());
            }
            
        }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem newspaper;
    private javax.swing.JMenuItem selectEngine;
    private javax.swing.JMenuItem selectTrainOrders;
    private javax.swing.JMenuItem selectWagons;
    private javax.swing.JMenu show;
    private javax.swing.JMenuItem showCargoWaitingAndDemand;
    private javax.swing.JMenuItem showControls;
    private javax.swing.JMenuItem showJavaSystemProperties;
    private javax.swing.JMenuItem showNetworthGraph;
    private javax.swing.JMenuItem showStationInfo;
    private javax.swing.JMenuItem showTerrainInfo;
    private javax.swing.JMenuItem showTrainList;
    // End of variables declaration//GEN-END:variables
    
}
