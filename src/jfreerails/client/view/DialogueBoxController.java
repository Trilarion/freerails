/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */

package jfreerails.client.view;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.border.LineBorder;

import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.Move;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;

/**	This class is responsible for displaying dialogue boxes, adding borders to them as appropriate, and
 *  returning focus to the last focus owner after a dialogue box has been closed.  It is also responsible for 
 * adding components that need to update in response to moves to the MoveChainFork.  Currently dialogue boxes
 * are not separate windows.  Instead, they are drawn on the modal layer of the main JFrames LayerPlane.  This
 * allows dialogue boxes with transparent regions to be used.
 *
 * @author  lindsal8
 */
public class DialogueBoxController {

	private JButton closeButton = new JButton("Close");
	private SelectEngineJPanel selectEngine;
	private MyGlassPanel glassPanel;
	private NewsPaperJPanel newspaper;
	private SelectWagonsJPanel selectWagons;
	//private TrainScheduleJPanel trainScheduleJPanel;
	private GameControlsJPanel showControls;
	private TerrainInfoJPanel terrainInfo;
	private StationInfoJPanel stationInfo;
	private JList trainList = new JList();
	private TrainDialogueJPanel trainDialogueJPanel;
	private ReadOnlyWorld world;
	private ViewLists viewLists;
	private UntriedMoveReceiver moveReceiver;
	private ModelRoot modelRoot;

	private Component defaultFocusOwner = null;

	private LineBorder defaultBorder =
		new LineBorder(new java.awt.Color(0, 0, 0), 3);

	/** Use this ActionListener to close a dialogue without performing any other action. */
	private ActionListener closeCurrentDialogue = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			closeContent();
		}
	};

	private CallBacks callbacks = new CallBacks() {
		public void closeDialogue() {
			closeContent();
		}

		public void moveCursor(int x, int y) {
			// TODO Auto-generated method stub			
		}

		public void processMove(Move m) {
			moveReceiver.processMove(m);
		}
	};

	/** Creates new DialogueBoxController */

	public DialogueBoxController(JFrame frame, ModelRoot mr) {
		modelRoot = mr;
		//Setup glass panel..
		glassPanel = new MyGlassPanel();
		glassPanel.setSize(frame.getSize());
		frame.getLayeredPane().add(glassPanel, JLayeredPane.MODAL_LAYER);
		glassPanel.revalidate();
		glassPanel.setVisible(false);

		//We need to resize the glass panel when its parent resizes.
		frame
			.getLayeredPane()
			.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				glassPanel.setSize(glassPanel.getParent().getSize());
				glassPanel.revalidate();
			}
		});

		closeButton.addActionListener(closeCurrentDialogue);
	}

	public void setup(
		ReadOnlyWorld w,
		ViewLists vl,
		MoveChainFork moveChainFork,
		UntriedMoveReceiver mr,
		MapCursor mapCursor) {
		
		moveReceiver = mr;

		if (w == null)
			throw new NullPointerException();
		if (vl == null)
			throw new NullPointerException();
		if (moveChainFork == null)
			throw new NullPointerException();
		if (mapCursor == null)
			throw new NullPointerException();

		this.world = w;
		viewLists = vl;

		//Setup the various dialogue boxes.

		// setup the terrain info dialogue.
		terrainInfo = new TerrainInfoJPanel();
		terrainInfo.setup(w, vl);

		// setup the supply and demand at station dialogue.
		stationInfo = new StationInfoJPanel();
		stationInfo.setup(w, vl);
		moveChainFork.add(stationInfo);
		stationInfo.setMapCursor(mapCursor);

		// setup the 'show controls' dialogue
		showControls = new GameControlsJPanel();
		showControls.setup(w, vl, this.closeCurrentDialogue);

		//Set up train orders dialogue
		//trainScheduleJPanel = new TrainScheduleJPanel();
		//trainScheduleJPanel.setup(w, vl);
		//moveChainFork.add(trainScheduleJPanel);

		//Set up select engine dialogue.
		selectEngine = new SelectEngineJPanel(this);
		selectEngine.setup(w, vl, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				showSelectWagons();
			}

		});
		newspaper = new NewsPaperJPanel();
		newspaper.setup(w, vl, closeCurrentDialogue);

		selectWagons = new SelectWagonsJPanel();

		final ReadOnlyWorld finalROW = this.world;
		//So that inner class can reference it.
		selectWagons.setup(w, vl, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				WorldIterator wi = new NonNullElements(KEY.STATIONS, finalROW);
				if (wi.next()) {

					StationModel station = (StationModel) wi.getElement();

					ProductionAtEngineShop before = station.getProduction();
					int engineType = selectEngine.getEngineType();
					int[] wagonTypes = selectWagons.getWagons();
					ProductionAtEngineShop after =
						new ProductionAtEngineShop(engineType, wagonTypes);

					Move m =
						new ChangeProductionAtEngineShopMove(
							before,
							after,
							wi.getIndex());
					moveReceiver.processMove(m);
				}
				closeContent();
			}

		});

		trainDialogueJPanel = new TrainDialogueJPanel();
		trainDialogueJPanel.setup(world, viewLists, callbacks);
		moveChainFork.addListListener(trainDialogueJPanel);
	}

	public void showNewspaper(String headline) {
		newspaper.setHeadline(headline);
		showContent(newspaper);
	}

	public void showTrainOrders() {
		WorldIterator wi = new NonNullElements(KEY.TRAINS, world);
		if (!wi.next()) {
			modelRoot.getUserMessageLogger().println("Cannot" +
				" show train orders since there are no" +
				" trains!");
		} else {
			trainDialogueJPanel.display(0);
			this.showContent(trainDialogueJPanel);
		}
	}

	public void showSelectEngine() {
		WorldIterator wi = new NonNullElements(KEY.STATIONS, world);
		if (!wi.next()) {
			modelRoot.getUserMessageLogger().println("Can't" +
			" build train since there are no stations");
		} else {
			
			showContent(selectEngine);
		}
	}

	public void showGameControls() {

		showContent(this.showControls);
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
		FreerailsTile tile = world.getTile(x, y);
		int terrainType = tile.getTerrainTypeNumber();
		showTerrainInfo(terrainType);
	}

	public void showStationInfo(int stationNumber) {
		try {
			stationInfo.setStation(stationNumber);
			showContent(stationInfo);
		} catch (NoSuchElementException e) {
			System.err.println("Station " + stationNumber + " does not exist!");
		}
	}

	public void showTrainList() {
		if (world.size(KEY.TRAINS) > 0) {
			trainList.setModel(new World2ListModelAdapter(world, KEY.TRAINS));
			TrainViewJPanel trainView =
				new TrainViewJPanel(world, viewLists, 0);
			trainList.setCellRenderer(trainView);
			trainView.setHeight(50);
			showContent(trainList);
		} else {
			modelRoot.getUserMessageLogger().println("There are" +
				" no trains to display!");
		}
	}

	public void showContent(JComponent component) {
		JComponent contentPanel;
		if (!(component instanceof View)) {
			contentPanel = new javax.swing.JPanel();
			contentPanel.setLayout(new java.awt.GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			contentPanel.add(component, constraints);

			constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 1;
			contentPanel.add(closeButton, constraints);
		} else {
			contentPanel = component;
		}

		contentPanel.setBorder(defaultBorder);
		//		if(!glassPanel.isVisible()){
		//			KeyboardFocusManager keyboardFocusManager =
		//			KeyboardFocusManager.getCurrentKeyboardFocusManager();
		//			lastFocusOwner = keyboardFocusManager.getFocusOwner();
		//		}
		glassPanel.showContent(contentPanel);
		glassPanel.validate();
		glassPanel.setVisible(true);
	}
	public void closeContent() {
		glassPanel.setVisible(false);
		if (null != defaultFocusOwner) {
			defaultFocusOwner.requestFocus();
		}
	}

	public Component getDefaultFocusOwner() {
		return defaultFocusOwner;
	}

	public void setDefaultFocusOwner(Component defaultFocusOwner) {
		this.defaultFocusOwner = defaultFocusOwner;
	}

	public void showStationOrTerrainInfo(int x, int y) {
		FreerailsTile tile = world.getTile(x, y);
		if (tile.getTrackRule().isStation()) {
			for (int i = 0; i < world.size(KEY.STATIONS); i++) {
				StationModel station =
					(StationModel) world.get(KEY.STATIONS, i);
				if (null != station && station.x == x && station.y == y) {
					this.showStationInfo(i);
					return;
				}
			}
			throw new IllegalStateException(
				"Could find station at " + x + ", " + y);
		} else {
			this.showTerrainInfo(x, y);
		}
	}

}
