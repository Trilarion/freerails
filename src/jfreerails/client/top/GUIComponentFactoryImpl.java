package jfreerails.client.top;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.view.CashJLabel;
import jfreerails.client.view.DateJLabel;
import jfreerails.client.view.DetailMapView;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MainMapAndOverviewMapMediator;
import jfreerails.client.view.MapCursor;
import jfreerails.client.view.MapViewJComponentConcrete;
import jfreerails.client.view.MapViewMoveReceiver;
import jfreerails.client.view.OverviewMapJComponent;
import jfreerails.client.view.TrainsJTabPane;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.ServerControlInterface;
import jfreerails.controller.StationBuilder;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.WorldChangedEvent;
import jfreerails.world.top.ReadOnlyWorld;

public class GUIComponentFactoryImpl
	implements GUIComponentFactory, MoveReceiver {

	private DateJLabel datejLabel;
	private CashJLabel cashjLabel;

	/**
	 * This is the panel at the bottom right of the screen
	 */
	private TrainsJTabPane trainsJTabPane;

	private javax.swing.JMenu helpMenu;
	private javax.swing.JLabel messageJLabel;

	private final DialogueBoxController dialogueBoxController;

	private ViewLists viewLists;
	private GUIClient client;
	private ReadOnlyWorld world;
	private MainMapAndOverviewMapMediator mediator;

	MapCursor cursor;
	UserInputOnMapController userInputOnMapController;

	StationTypesPopup stationTypesPopup;
	BuildMenu buildMenu;
	JMenu displayMenu;
	JPanel overviewMapContainer;
	JScrollPane mainMapContainer;
	MapViewJComponentConcrete mapViewJComponent;
	TrackMoveProducer trackBuilder;
	private JScrollPane mainMapScrollPane1;
	MapRenderer overviewMap;
	DetailMapView mainMap;

	Rectangle r = new Rectangle(10, 10, 10, 10);

	JFrame clientJFrame;

	public GUIComponentFactoryImpl() {
		userInputOnMapController = new UserInputOnMapController();
		buildMenu = new jfreerails.client.top.BuildMenu();
		mapViewJComponent = new MapViewJComponentConcrete();
		mainMapScrollPane1 = new JScrollPane();
		overviewMapContainer = new OverviewMapJComponent(r);
		stationTypesPopup = new StationTypesPopup();
		this.mediator =
			new MainMapAndOverviewMapMediator(
				overviewMapContainer,
				mainMapScrollPane1.getViewport(),
				mapViewJComponent,
				r);

		//glassPanel = new MyGlassPanel();
		//glassPanel.showContent(new NewsPaperJPanel());

		//clientJFrame.setGlassPane(glassPanel);

		trainsJTabPane = new TrainsJTabPane();
		datejLabel = new DateJLabel();

		cashjLabel = new CashJLabel();
		messageJLabel = new javax.swing.JLabel("Message");

		clientJFrame = new ClientJFrame(this);
		dialogueBoxController = new DialogueBoxController(clientJFrame);

	}

	public void setup(ViewLists vl, GUIClient c) {
		
		
		viewLists = vl;
		client = c;
		world = client.getWorld();
		if (!vl.validate(world)) {
			throw new IllegalArgumentException(
				"The specified"
					+ " ViewLists are not comaptible with the clients"
					+ "world!");
		}

		

		//create the main and overview maps
		mainMap = new DetailMapView(world, viewLists);
		overviewMap = new ZoomedOutMapRenderer(world);

		//init the move handlers

		MoveReceiver overviewmapMoveReceiver = new MapViewMoveReceiver(mainMap);

		MoveChainFork moveFork = client.getMoveChainFork();
		moveFork.add(overviewmapMoveReceiver);		

		MoveReceiver mainmapMoveReceiver = new MapViewMoveReceiver(overviewMap);
		moveFork.add(mainmapMoveReceiver);

		UntriedMoveReceiver receiver = client.getReceiver();

		trackBuilder = new TrackMoveProducer(world, receiver);
		StationBuilder sb = new StationBuilder(receiver, client.getWorld());

		stationTypesPopup.setup(sb, mainMap.getStationRadius());

		mapViewJComponent.setup(mainMap, client.getWorld());
		client.setCursor(mapViewJComponent.getMapCursor());
		this.cursor = client.getCursor();
		//setup the the main and overview map JComponents

		dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

		userInputOnMapController.setup(
			mapViewJComponent,
			trackBuilder,
			stationTypesPopup,
			client,
			dialogueBoxController,
			receiver);

		buildMenu.setup(world, trackBuilder);
		mainMapScrollPane1.setViewportView(this.mapViewJComponent);
		System.out.println("Viewport was set");
		((OverviewMapJComponent) overviewMapContainer).setup(overviewMap);

		datejLabel.setup(world, null, null);
		cashjLabel.setup(world, null, null);
		trainsJTabPane.setup(world, vl);
		MapCursor mapCursor = c.getCursor();
		mapCursor.addCursorEventListener(trainsJTabPane);
		trainsJTabPane.setMapCursor(mapCursor);		
		dialogueBoxController.setup(world, vl, client.moveChainFork, mapCursor);
	}

	public JPanel createOverviewMap() {
		return overviewMapContainer;
	}

	public JScrollPane createMainMap() {
		return mainMapScrollPane1;
	}

	public JMenu createBuildMenu() {
		return buildMenu;
	}

	public JMenu createDisplayMenu() {
		displayMenu = new JMenu("Display");
		displayMenu.setMnemonic(68);
		JMenuItem trainOrdersJMenuItem = new JMenuItem("Train Orders");
		trainOrdersJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogueBoxController.showTrainOrders();
			}
		});

		JMenuItem stationInfoJMenuItem = new JMenuItem("Station Info");
		stationInfoJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogueBoxController.showStationInfo(0);
			}
		});
		
		JMenuItem trainListJMenuItem = new JMenuItem("Train List");
		trainListJMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialogueBoxController.showTrainList();
					}
		});
		
		//		I've moved the processing to the menu item above, LL		
		//		JMenuItem stationInfoCalculations = new JMenuItem("Calculate Station Info");
		//		stationInfoCalculations.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				CalcSupplyAtStations cSAS = new CalcSupplyAtStations(world);
		//				cSAS.doProcessing();
		//			}
		//		});

		displayMenu.add(trainOrdersJMenuItem);
		displayMenu.add(stationInfoJMenuItem);
		displayMenu.add(trainListJMenuItem);
		//displayMenu.add(stationInfoCalculations);

		return displayMenu;
	}

	public class GameSpeedListener implements ActionListener {
		private int speed;

		public GameSpeedListener(int speed) {
			this.speed = speed;
		}

		public void actionPerformed(ActionEvent e) {
			ServerControlInterface sc = client.getServerControls();
			if (sc != null) {
				sc.setTargetTicksPerSecond(speed);
			}
		}
	}

	public JMenu createGameMenu() {

		JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic(71);

		JMenuItem quitJMenuItem = new JMenuItem("Exit Game");
		quitJMenuItem.setMnemonic(88);

		quitJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}

		});

		JMenuItem newGameJMenuItem = new JMenuItem("New game big map");

		newGameJMenuItem.addActionListener(new ActionListener() {
			String mapName = "south_america";

			public void actionPerformed(ActionEvent e) {
				ServerControlInterface sc = client.getServerControls();
				if (sc != null) {
					sc.newGame(mapName);
					worldModelChanged();
				}
			}
		});

		JMenuItem newGameJMenuItem2 = new JMenuItem("New game small map");

		newGameJMenuItem2.addActionListener(new ActionListener() {
			String mapName = "small_south_america";

			public void actionPerformed(ActionEvent e) {
				ServerControlInterface sc = client.getServerControls();
				if (sc != null) {
					sc.newGame(mapName);
					worldModelChanged();
				}
			}

		});

		JMenuItem saveGameJMenuItem = new JMenuItem("Save game");
		saveGameJMenuItem.setMnemonic(83);

		saveGameJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ServerControlInterface sc = client.getServerControls();
				if (sc != null) {
					sc.saveGame();
				}
			}

		});

		JMenuItem loadGameJMenuItem = new JMenuItem("Load game");
		loadGameJMenuItem.setMnemonic(76);

		loadGameJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ServerControlInterface sc = client.getServerControls();
				if (sc != null) {
					sc.loadGame();
					worldModelChanged();
				}
			}

		});

		JMenuItem newspaperJMenuItem = new JMenuItem("Newspaper");
		newspaperJMenuItem.setMnemonic(78);

		newspaperJMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dialogueBoxController.showNewspaper("Headline");
				//glassPanel.setVisible(true);
			}

		});

		//Set up the gamespeed submenu.
		ButtonGroup group = new ButtonGroup();
		JMenu gameSpeedSubMenu = new JMenu("Game Speed...");

		JRadioButtonMenuItem paused = new JRadioButtonMenuItem("Frozen");
		group.add(paused);
		gameSpeedSubMenu.add(paused);
		paused.addActionListener(new GameSpeedListener(0));

		JRadioButtonMenuItem slow = new JRadioButtonMenuItem("Slow");
		group.add(slow);
		gameSpeedSubMenu.add(slow);
		slow.addActionListener(new GameSpeedListener(10));

		JRadioButtonMenuItem moderate = new JRadioButtonMenuItem("Moderate");
		group.add(moderate);
		gameSpeedSubMenu.add(moderate);
		moderate.addActionListener(new GameSpeedListener(30));

		//Set the initial game speed to moderate.
		moderate.setSelected(true);

		JRadioButtonMenuItem fast = new JRadioButtonMenuItem("Fast");
		group.add(fast);
		gameSpeedSubMenu.add(fast);
		fast.addActionListener(new GameSpeedListener(50));

		JRadioButtonMenuItem turbo = new JRadioButtonMenuItem("Turbo");
		group.add(turbo);
		gameSpeedSubMenu.add(turbo);
		turbo.addActionListener(new GameSpeedListener(50));

		gameMenu.add(newGameJMenuItem);
		gameMenu.add(newGameJMenuItem2);
		gameMenu.addSeparator();
		gameMenu.add(loadGameJMenuItem);
		gameMenu.add(saveGameJMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(gameSpeedSubMenu);
		gameMenu.add(newspaperJMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(quitJMenuItem);

		return gameMenu;
	}

	private void addMainMapAndOverviewMapMediatorIfNecessary() {
		//if (this.mainMapContainer != null
		//	&& this.overviewMapContainer != null
		//	&& null == this.mediator) {
		//	//Rectangle r = this.overviewMapContainer.getMainMapVisibleRect();
		//
		//}
	}

	ViewLists getViewLists() {
		return this.viewLists;
	}

	ReadOnlyWorld getAddTrackRules() {
		return this.world;
	}

	public JFrame createClientJFrame() {
		return clientJFrame;

	}

	public JMenu createHelpMenu() {

		helpMenu = new javax.swing.JMenu("Help");
		JMenuItem showControls = new JMenuItem("Show game controls");
		showControls.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dialogueBoxController.showGameControls();
			}
		});

		helpMenu.add(showControls);
		return helpMenu;
	}

	public JTabbedPane createTrainsJTabPane() {
		return trainsJTabPane;
	}

	public JLabel createCashJLabel() {
		return cashjLabel;
	}

	public JLabel createMessagePanel() {
		return messageJLabel;
	}

	public JLabel createDateJLabel() {
		return datejLabel;
	}

	private void worldModelChanged() {
		/*
		 * XXX this is temporary - we should have a formal object to store
		 * the clients copy of the model, connections to the server, etc.
		 */
		ReadOnlyWorld world = client.getWorld();
		ViewLists viewLists = getViewLists();

		if (!viewLists.validate(world)) {
			throw new IllegalArgumentException();
		}
		setup(viewLists, client);
	}

	public void processMove(Move m) {
		if (m instanceof WorldChangedEvent) {
			worldModelChanged();
		}
	}
}
