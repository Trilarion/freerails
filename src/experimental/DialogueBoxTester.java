package experimental;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JList;

import jfreerails.client.common.JFrameMinimumSizeEnforcer;
import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.top.ViewListsImpl;
import jfreerails.client.view.CallBacks;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MapCursor;
import jfreerails.client.view.ModelRoot;
import jfreerails.client.view.NewTrainScheduleJPanel;
import jfreerails.client.view.TrainDialogueJPanel;
import jfreerails.client.view.TrainOrdersListModel;
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
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
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
public class DialogueBoxTester extends javax.swing.JFrame implements CallBacks {
    
    private final DialogueBoxController dialogueBoxController;
    
    private Random randy = new Random(System.currentTimeMillis());
    
    private World w;
    
    private ViewLists vl;

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
		move.doMove(w);		
	}
    };
    
    TrainDialogueJPanel trainDialogueJPanel = new TrainDialogueJPanel();
    
    /** Creates new form TestGlassPanelMethod */
    public DialogueBoxTester() {
        
	ModelRoot mr = new ModelRoot();
        w = new WorldImpl();
		//MapFixtureFactory.generateTrackRuleList(w);
	try {
	    vl = new ViewListsImpl(w, FreerailsProgressMonitor.NULL_INSTANCE);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	mr.setWorld(w, dummyReceiver, vl);
        dialogueBoxController = new DialogueBoxController(this, mr);
        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        TileSetFactory tileFactory = new NewTileSetFactoryImpl();
        tileFactory.addTerrainTileTypesList(w);
        wetf.addTypesToWorld(w);
        int numberOfCargoTypes = w.size(KEY.CARGO_TYPES);
        
        w.add(
        KEY.STATIONS,
        new StationModel(10, 10, "Bristol", numberOfCargoTypes, 0));
        w.add(
        KEY.STATIONS,
        new StationModel(10, 10, "Bath", numberOfCargoTypes, 0));
        w.add(
        KEY.STATIONS,
        new StationModel(10, 10, "Cardiff", numberOfCargoTypes, 0));
        w.add(
        KEY.STATIONS,
        new StationModel(10, 10, "London", numberOfCargoTypes, 0));
        w.add(
        KEY.STATIONS,
        new StationModel(10, 10, "Swansea", numberOfCargoTypes, 0));
        //Set up cargo bundle, for the purpose of this test code all the trains can share the
        //same one.
        CargoBundle cb = new CargoBundleImpl();
        cb.setAmount(new CargoBatch(0, 10, 10, 8, 0), 10);
        cb.setAmount(new CargoBatch(0, 10, 10, 9, 0), 10);
        cb.setAmount(new CargoBatch(1, 10, 10, 9, 0), 10);
        cb.setAmount(new CargoBatch(3, 10, 10, 9, 0), 10);
        cb.setAmount(new CargoBatch(5, 10, 10, 9, 0), 10);
        w.add(KEY.CARGO_BUNDLES, cb);
        
        MutableSchedule schedule = new MutableSchedule();
        TrainOrdersModel order =
        new TrainOrdersModel(0, new int[] { 0, 0, 0 }, false);
        TrainOrdersModel order2 =
        new TrainOrdersModel(0, new int[] { 1, 2, 0, 0,0 }, true);
        TrainOrdersModel order3 =
        new TrainOrdersModel(0, null, true);        
        schedule.setOrder(0, order);
        schedule.setOrder(1, order);
        
        int scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule());        
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 0, 0 }, null, scheduleID));        
        schedule.setOrder(2, order2);
        schedule.setOrder(3, order3);
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule());
        w.add(KEY.TRAINS, new TrainModel(1, new int[] { 1, 1 }, null, scheduleID));
        schedule.setOrder(4, order2);
        schedule.setOrderToGoto(3);
        schedule.setPriorityOrders(order);        
        scheduleID = w.add(KEY.TRAIN_SCHEDULES, schedule.toImmutableSchedule());
        w.add(KEY.TRAINS, new TrainModel(0, new int[] { 1, 2, 0 }, null, scheduleID));
        
            
	final MyGlassPanel glassPanel = new MyGlassPanel();
	dialogueBoxController.setup(
		w,
		vl,
		new MoveChainFork(),
		dummyReceiver,
		MapCursor.NULL_MAP_CURSOR);
	initComponents();

	glassPanel.setSize(800, 600);
	//this.setGlassPane(glassPanel);
	this.addComponentListener(new JFrameMinimumSizeEnforcer(640, 400));
	//pack();
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

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jfreerails/data/south_america.png")));
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

        jMenuBar1.add(show);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents
    
    private void trainScheduleJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trainScheduleJMenuItemActionPerformed
        // Add your handling code here:
        NewTrainScheduleJPanel tsp = new NewTrainScheduleJPanel();
        tsp.setup(w, vl, this);
        tsp.display(0);
        dialogueBoxController.showContent(tsp);
    }//GEN-LAST:event_trainScheduleJMenuItemActionPerformed
    
    private void showNewTrainOrdersJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNewTrainOrdersJMenuItemActionPerformed
        // Add your handling code here:
        JList list = new JList();
        jfreerails.client.view.TrainOrderJPanel trainOrderJPanel = new  jfreerails.client.view.TrainOrderJPanel();
        trainOrderJPanel.setup(w, vl, null);
        list.setCellRenderer(trainOrderJPanel);
        TrainOrdersListModel listModel = new TrainOrdersListModel(w, 0);
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
            int trainNumber = randy.nextInt(w.size(KEY.TRAINS) - 1);
            JComponent trainView = new TrainViewJList(w, vl, trainNumber);
            dialogueBoxController.showContent(trainView);
	} //GEN-LAST:event_showTrainConsistActionPerformed
        
	private void showStationInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStationInfoActionPerformed
            // Add your handling code here:
            int stationNumber = randy.nextInt(w.size(KEY.STATIONS) - 1);
            dialogueBoxController.showStationInfo(stationNumber);
	} //GEN-LAST:event_showStationInfoActionPerformed
        
	private void showTerrainInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTerrainInfoActionPerformed
            // Add your handling code here:
            int terrainType = randy.nextInt(w.size(KEY.TERRAIN_TYPES) - 1);
            dialogueBoxController.showTerrainInfo(terrainType);
	} //GEN-LAST:event_showTerrainInfoActionPerformed
        
	private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showControlsActionPerformed
            // Add your handling code here:
            dialogueBoxController.showGameControls();
	} //GEN-LAST:event_showControlsActionPerformed
        
	private void selectTrainOrdersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTrainOrdersActionPerformed
            // Add your handling code here:
            trainDialogueJPanel.setup(w, vl, this);
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
        
        public void closeDialogue() {
            // TODO Auto-generated method stub
            
        }
        
        
        public void moveCursor(int x, int y) {
            // TODO Auto-generated method stub
            
        }
        
        
        public void processMove(Move m) {
            MoveStatus ms = m.doMove(w);
            if(!ms.ok){
                throw new IllegalArgumentException(ms.message);
            }
            if(m instanceof ListMove){
                ListMove lm = (ListMove)m;
                trainDialogueJPanel.listUpdated(lm.getKey(), lm.getIndex());
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
    private javax.swing.JMenuItem showControls;
    private javax.swing.JMenuItem showNewTrainOrdersJMenuItem;
    private javax.swing.JMenuItem showStationInfo;
    private javax.swing.JMenuItem showTerrainInfo;
    private javax.swing.JMenuItem showTrainConsist;
    private javax.swing.JMenuItem showTrainList;
    private javax.swing.JMenuItem trainScheduleJMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
