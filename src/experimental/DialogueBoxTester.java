package experimental;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JList;

import jfreerails.client.common.JFrameMinimumSizeEnforcer;
import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.top.ViewListsImpl;
import jfreerails.client.view.CargoWaitingAndDemandedJPanel;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.HtmlJPanel;
import jfreerails.client.view.MapCursor;
import jfreerails.client.view.ModelRoot;
import jfreerails.client.view.SelectStationJPanel;
import jfreerails.client.view.ShowJavaProperties;
import jfreerails.client.view.TrainDialogueJPanel;
import jfreerails.client.view.TrainOrdersListModel;
import jfreerails.client.view.TrainScheduleJPanel;
import jfreerails.client.view.TrainViewJList;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.ListMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.server.NewTileSetFactoryImpl;
import jfreerails.server.common.TileSetFactory;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
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
    
    private Random randy = new Random(System.currentTimeMillis());
    
    private World w;
    
    private ViewLists vl;
    
    private ModelRoot modelRoot;
    
    private ActionListener closeCurrentDialogue = new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            dialogueBoxController.closeContent();
        }
    };
    
    private UntriedMoveReceiver dummyReceiver = new UntriedMoveReceiver() {
	public MoveStatus tryDoMove(Move move) {
	    return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(Move move) {
	    return MoveStatus.MOVE_OK;
	}

	public void undoLastMove() {
	}

	public void processMove(Move move) {
		move.doMove(w, Player.AUTHORITATIVE);		
	}
    };
    
    TrainDialogueJPanel trainDialogueJPanel = new TrainDialogueJPanel();
    
    /** Creates new form TestGlassPanelMethod */
    public DialogueBoxTester() {
        
    	modelRoot = new ModelRoot();
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
        modelRoot.setup(w, dummyReceiver, vl, TEST_PLAYER.getPrincipal());		
        dialogueBoxController = new DialogueBoxController(this, modelRoot);
        dialogueBoxController.setDefaultFocusOwner(this);
        
        int numberOfCargoTypes = w.size(SKEY.CARGO_TYPES);
        StationModel bristol = new StationModel(10, 10, "Bristol", numberOfCargoTypes, 0);
        boolean [] demandArray = new boolean [numberOfCargoTypes];
        demandArray[0] = true;
        demandArray[2] = true;
        demandArray[3] = true;
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
        CargoBundle cb = new CargoBundleImpl();
        cb.setAmount(new CargoBatch(0, 10, 10, 8, 0), 80);
        cb.setAmount(new CargoBatch(0, 10, 10, 9, 0), 60);
        cb.setAmount(new CargoBatch(1, 10, 10, 9, 0), 140);
        cb.setAmount(new CargoBatch(3, 10, 10, 9, 0), 180);
        cb.setAmount(new CargoBatch(5, 10, 10, 9, 0), 10);
        w.add(KEY.CARGO_BUNDLES, cb, TEST_PRINCIPAL);
        
        MutableSchedule schedule = new MutableSchedule();
        TrainOrdersModel order =
        new TrainOrdersModel(0, new int[] { 0, 0, 0 }, false);
        TrainOrdersModel order2 =
        new TrainOrdersModel(1, new int[] { 1, 2, 0, 0,0 }, true);
        TrainOrdersModel order3 =
        new TrainOrdersModel(2, null, true);
        schedule.setOrder(0, order);
        schedule.setOrder(1, order2);
        
        int scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 0, 0 }, null, scheduleID), TEST_PRINCIPAL);
        schedule.setOrder(2, order2);
        schedule.setOrder(3, order3);
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(1, new int[] { 1, 1 }, null, scheduleID), TEST_PRINCIPAL);
        schedule.setOrder(4, order2);
        schedule.setOrderToGoto(3);
        schedule.setPriorityOrders(order);
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule(), TEST_PRINCIPAL);
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 1, 2, 0 }, null, scheduleID), TEST_PRINCIPAL);
        
            
	final MyGlassPanel glassPanel = new MyGlassPanel();
	dialogueBoxController.setup(
		w,
		vl,
		new MoveChainFork(),
		dummyReceiver,
		MapCursor.NULL_MAP_CURSOR);
	initComponents();

	glassPanel.setSize(800, 600);
	this.addComponentListener(new JFrameMinimumSizeEnforcer(640, 400));

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
        showTrainConsist = new javax.swing.JMenuItem();
        showTrainList = new javax.swing.JMenuItem();
        showNewTrainOrdersJMenuItem = new javax.swing.JMenuItem();
        trainScheduleJMenuItem = new javax.swing.JMenuItem();
        showSelectStation = new javax.swing.JMenuItem();
        showCargoWaitingAndDemand = new javax.swing.JMenuItem();
        showJavaSystemProperties = new javax.swing.JMenuItem();

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

        showTrainConsist.setText("Train Consist");
        showTrainConsist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrainConsistActionPerformed(evt);
            }
        });

        show.add(showTrainConsist);

        showTrainList.setText("Train List");
        showTrainList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTrainListActionPerformed(evt);
            }
        });

        show.add(showTrainList);

        showNewTrainOrdersJMenuItem.setText("New  train orders");
        showNewTrainOrdersJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNewTrainOrdersJMenuItemActionPerformed(evt);
            }
        });

        show.add(showNewTrainOrdersJMenuItem);

        trainScheduleJMenuItem.setText("Train Schedule");
        trainScheduleJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trainScheduleJMenuItemActionPerformed(evt);
            }
        });

        show.add(trainScheduleJMenuItem);

        showSelectStation.setText("Select Station From Map");
        showSelectStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSelectStationActionPerformed(evt);
            }
        });

        show.add(showSelectStation);

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

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents

    private void showJavaSystemPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavaSystemPropertiesActionPerformed
        // Add your handling code here:
        String s = ShowJavaProperties.getPropertiesHtmlString();
        System.out.println(s);
        HtmlJPanel htmlPanel = new HtmlJPanel(s);
       htmlPanel.setup(modelRoot, closeCurrentDialogue);
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
        panel.setup(modelRoot, closeCurrentDialogue);
        int newStationID = randy.nextInt(w.size(KEY.STATIONS, TEST_PRINCIPAL) - 1);
        panel.display(newStationID);
        dialogueBoxController.showContent(panel);
    }//GEN-LAST:event_showCargoWaitingAndDemandActionPerformed
    
    private void showSelectStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSelectStationActionPerformed
        // Add your handling code here:
        SelectStationJPanel selectStation = new SelectStationJPanel();
        selectStation.setup(modelRoot, closeCurrentDialogue);
        dialogueBoxController.showContent(selectStation);
    }//GEN-LAST:event_showSelectStationActionPerformed
    
    private void trainScheduleJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainScheduleJMenuItemActionPerformed
        // Add your handling code here:
        TrainScheduleJPanel tsp = new TrainScheduleJPanel();
        tsp.setup(modelRoot, null);
        tsp.display(0);
        dialogueBoxController.showContent(tsp);
    }//GEN-LAST:event_trainScheduleJMenuItemActionPerformed
    
    private void showNewTrainOrdersJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNewTrainOrdersJMenuItemActionPerformed
        // Add your handling code here:
        JList list = new JList();
        jfreerails.client.view.TrainOrderJPanel trainOrderJPanel = new  jfreerails.client.view.TrainOrderJPanel();
        trainOrderJPanel.setup(modelRoot, null);
        list.setCellRenderer(trainOrderJPanel);
        TrainOrdersListModel listModel = new TrainOrdersListModel(w, 0, TEST_PRINCIPAL);
        list.setModel(listModel);
        list.setFixedCellWidth(250);
        dialogueBoxController.showContent(list);
        
    }//GEN-LAST:event_showNewTrainOrdersJMenuItemActionPerformed
    
    private void showTrainListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrainListActionPerformed
        // Add your handling code here:
        dialogueBoxController.showTrainList();
    }//GEN-LAST:event_showTrainListActionPerformed
    
    
    
	private void showTrainConsistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTrainConsistActionPerformed
            // Add your handling code here:
            //TrainView
            int trainNumber = randy.nextInt(w.size(KEY.TRAINS, TEST_PRINCIPAL) - 1);
            JComponent trainView = new TrainViewJList(modelRoot, trainNumber);
            dialogueBoxController.showContent(trainView);
	} //GEN-LAST:event_showTrainConsistActionPerformed
        
	private void showStationInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStationInfoActionPerformed
            // Add your handling code here:
            int stationNumber = randy.nextInt(w.size(KEY.STATIONS, TEST_PRINCIPAL) - 1);
            dialogueBoxController.showStationInfo(stationNumber);
	} //GEN-LAST:event_showStationInfoActionPerformed
        
	private void showTerrainInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTerrainInfoActionPerformed
            // Add your handling code here:
            int terrainType = randy.nextInt(w.size(SKEY.TERRAIN_TYPES) - 1);
            dialogueBoxController.showTerrainInfo(terrainType);
	} //GEN-LAST:event_showTerrainInfoActionPerformed
        
	private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showControlsActionPerformed
            // Add your handling code here:
            dialogueBoxController.showGameControls();
	} //GEN-LAST:event_showControlsActionPerformed
        
	private void selectTrainOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTrainOrdersActionPerformed
            // Add your handling code here:
            trainDialogueJPanel.setup(modelRoot, closeCurrentDialogue);
            trainDialogueJPanel.display(0);
            dialogueBoxController.showContent(trainDialogueJPanel);
	} //GEN-LAST:event_selectTrainOrdersActionPerformed
        
	private void selectWagonsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectWagonsActionPerformed
            // Add your handling code here:
            dialogueBoxController.showSelectWagons();
	} //GEN-LAST:event_selectWagonsActionPerformed
        
	private void selectEngineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectEngineActionPerformed
            // Add your handling code here:
            dialogueBoxController.showSelectEngine();
	} //GEN-LAST:event_selectEngineActionPerformed
        
	private void newspaperActionPerformed(java.awt.event.ActionEvent evt) {	//GEN-FIRST:event_newspaperActionPerformed
            // Add your handling code here:
            dialogueBoxController.showNewspaper("New headline!");
	} //GEN-LAST:event_newspaperActionPerformed
        
        /** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) { //GEN-FIRST:event_exitForm
            System.exit(0);
	} //GEN-LAST:event_exitForm
        
        /**
         * @param args the command line arguments
         */
        public static void main(String args[]) {
            DialogueBoxTester test = new DialogueBoxTester();
            
            test.setSize(new Dimension(640, 400));
            test.show();
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
    private javax.swing.JMenuItem showNewTrainOrdersJMenuItem;
    private javax.swing.JMenuItem showSelectStation;
    private javax.swing.JMenuItem showStationInfo;
    private javax.swing.JMenuItem showTerrainInfo;
    private javax.swing.JMenuItem showTrainConsist;
    private javax.swing.JMenuItem showTrainList;
    private javax.swing.JMenuItem trainScheduleJMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
