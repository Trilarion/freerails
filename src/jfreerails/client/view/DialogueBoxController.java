/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */
package jfreerails.client.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.common.ModelRoot.Property;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.FinancialDataGatherer;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.IssueStockTransaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;

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

	private SelectEngineJPanel selectEngine;

	private final MyGlassPanel glassPanel;

	private NewsPaperJPanel newspaper;

	private SelectWagonsJPanel selectWagons;

	private HtmlJPanel showControls;

	private HtmlJPanel about;

	private HtmlJPanel how2play;

	private HtmlJPanel javaProperties;

	private TerrainInfoJPanel terrainInfo;

	private StationInfoJPanel stationInfo;

	private TrainDialogueJPanel trainDialogueJPanel;

	private BrokerScreenHtmlJFrame brokerScreenHtmlJFrame;

	private ReadOnlyWorld world;

	private ModelRootImpl modelRoot;

	private ViewLists vl;

	private Component defaultFocusOwner = null;

	private FinancialDataGatherer financialDataGatherer;

	private final JFrame frame;

	private JInternalFrame dialogueJInternalFrame;

	/**
	 * Use this ActionListener to close a dialogue without performing any other
	 * action.
	 */
	private final ActionListener closeCurrentDialogue = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			closeContent();
		}
	};

	private final ActionListener selectEngineActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			showSelectWagons();
		}
	};

	private final ActionListener trainDetailsButtonActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			showTrainList();
		}
	};

	private final ActionListener selectWagonsActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			WorldIterator wi = new NonNullElements(KEY.STATIONS, modelRoot
					.getWorld(), modelRoot.getPrincipal());

			if (wi.next()) {
				StationModel station = (StationModel) wi.getElement();

				ProductionAtEngineShop[] before = station.getProduction();
				int engineType = selectEngine.getEngineType();
				int[] wagonTypes = selectWagons.getWagons();
				ProductionAtEngineShop[] after = new ProductionAtEngineShop[] { new ProductionAtEngineShop(
						engineType, wagonTypes) };

				Move m = new ChangeProductionAtEngineShopMove(before, after, wi
						.getIndex(), modelRoot.getPrincipal());
				modelRoot.doMove(m);
			}
			closeContent();
		}
	};

	private final ActionListener issueBondActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {

			if (financialDataGatherer.canIssueBond()) {
				Move bondTransaction = new AddTransactionMove(modelRoot
						.getPrincipal(),
						BondTransaction.issueBond(financialDataGatherer
								.nextBondInterestRate()));
				modelRoot.doMove(bondTransaction);
			}
		}
	};

	private final ActionListener repayBondActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {

			Move bondTransaction = new AddTransactionMove(modelRoot
					.getPrincipal(), BondTransaction.repayBond(5));
			modelRoot.doMove(bondTransaction);
		}
	};

	private final ActionListener buyTreasuryStockActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {

			Move StockTransaction = new AddTransactionMove(modelRoot
					.getPrincipal(), IssueStockTransaction.issueStock(modelRoot
					.getWorld().getID(modelRoot.getPrincipal()), 10000,
					financialDataGatherer.sharePrice()));
			modelRoot.doMove(StockTransaction);
		}
	};

	private final ActionListener sellTreasuryStockActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			Move StockTransaction = new AddTransactionMove(modelRoot
					.getPrincipal(), IssueStockTransaction.sellStock(modelRoot
					.getWorld().getID(modelRoot.getPrincipal()), 10000,
					financialDataGatherer.sharePrice()));
			modelRoot.doMove(StockTransaction);
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
					public void componentResized(
							java.awt.event.ComponentEvent evt) {
						glassPanel.setSize(glassPanel.getParent().getSize());
						glassPanel.revalidate();
					}
				});

		closeButton.addActionListener(closeCurrentDialogue);

		showControls = new HtmlJPanel(DialogueBoxController.class
				.getResource("/jfreerails/client/view/game_controls.html"));
		about = new HtmlJPanel(DialogueBoxController.class
				.getResource("/jfreerails/client/view/about.htm"));
		how2play = new HtmlJPanel(DialogueBoxController.class
				.getResource("/jfreerails/client/view/how_to_play.htm"));

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
		brokerScreenHtmlJFrame = new BrokerScreenHtmlJFrame();

	}

	/**
	 * Called when a new game is started or a game is loaded.
	 * <p>
	 * <b>Be extremely careful with the references of objects allocated in this
	 * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
	 * starting several new games). </b>
	 * </p>
	 */
	public void setup(ModelRootImpl mr, ViewLists vl) {
		this.modelRoot = mr;
		this.vl = vl;
		modelRoot.addListListener(this); // When a new train gets built, we
											// show the train info etc

		this.world = modelRoot.getWorld();

		if (world == null)
			throw new NullPointerException();

		if (vl == null)
			throw new NullPointerException();

		financialDataGatherer = new FinancialDataGatherer(modelRoot.getWorld(),
				modelRoot.getPrincipal());

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
		selectEngine.setup(modelRoot, vl, selectEngineActionListener);

		newspaper.setup(modelRoot, vl, closeCurrentDialogue);

		selectWagons.setup(modelRoot, vl, selectWagonsActionListener);

		trainDialogueJPanel.setup(modelRoot, vl, this.closeCurrentDialogue);
		modelRoot.addListListener(trainDialogueJPanel);
		trainDialogueJPanel
				.setTrainDetailsButtonActionListener(trainDetailsButtonActionListener);
		trainDialogueJPanel
				.setCancelButtonActionListener(this.closeCurrentDialogue);

		// Sets up the BrokerScreen and Adds ActionListeners to the Menu
		brokerScreenHtmlJFrame
				.setIssueBondActionListener(this.issueBondActionListener);
		brokerScreenHtmlJFrame
				.setRepayBondActionListener(this.repayBondActionListener);
		brokerScreenHtmlJFrame
				.setBuytreasuryStockActionListener(this.buyTreasuryStockActionListener);
		brokerScreenHtmlJFrame
				.setSellTreasuryStockActionlistener(this.sellTreasuryStockActionListener);

		// for every player this is seting up an ActionListener to Buy and Sell
		// there stock
		int numberOfPlayers = modelRoot.getWorld().getNumberOfPlayers();
		for (int i = 0; i < numberOfPlayers; i++) {
			final Player temp = modelRoot.getWorld().getPlayer(i);
			@SuppressWarnings("unused") FinancialDataGatherer finacialDataGatherer = new FinancialDataGatherer(
					world, temp.getPrincipal());
			if (temp != null
					&& !(modelRoot.getPrincipal().equals(temp.getPrincipal()))) {
				brokerScreenHtmlJFrame.enableBuyPlayerStock(temp);
				brokerScreenHtmlJFrame.enableSellPlayerStock(temp);
			}

			brokerScreenHtmlJFrame.setBuyPlayerStockActionlistener(
					new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							Move StockTransaction = new AddTransactionMove(
									modelRoot.getPrincipal(),
									IssueStockTransaction.issueStock(modelRoot
											.getWorld().getID(
													temp.getPrincipal()),
											10000, (new FinancialDataGatherer(
													modelRoot.getWorld(), temp
															.getPrincipal()))
													.sharePrice()));
							modelRoot.doMove(StockTransaction);
							Move buyPlayerStock = new AddTransactionMove(temp
									.getPrincipal(), IssueStockTransaction
									.buyPlayerStock(modelRoot.getWorld().getID(
											modelRoot.getPrincipal()), 10000));
							modelRoot.doMove(buyPlayerStock);
						}
					}, temp);
			brokerScreenHtmlJFrame.setSellPlayerStockActionlistener(
					new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							Move StockTransaction = new AddTransactionMove(
									modelRoot.getPrincipal(),
									IssueStockTransaction.sellStock(modelRoot
											.getWorld().getID(
													temp.getPrincipal()),
											10000, (new FinancialDataGatherer(
													modelRoot.getWorld(), temp
															.getPrincipal()))
													.sharePrice()));
							modelRoot.doMove(StockTransaction);
							Move sellPlayerStock = new AddTransactionMove(temp
									.getPrincipal(), IssueStockTransaction
									.sellPlayerStock(modelRoot.getWorld()
											.getID(modelRoot.getPrincipal()),
											10000));
							modelRoot.doMove(sellPlayerStock);
						}
					}, temp);
		}
	}

	public void showNewspaper(String headline) {
		newspaper.setHeadline(headline);
		showContent(newspaper);
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

	public void showBrokerScreen() {
		// this is Creating a BrokerScreen Internal Frame in the Main Frame
		brokerScreenHtmlJFrame.setup(this.modelRoot, vl,
				this.closeCurrentDialogue);
		brokerScreenHtmlJFrame.setFrameIcon(null);

		int parentWidth = frame.getContentPane().getWidth();
		int parentHeight = frame.getContentPane().getHeight();

		Dimension size = brokerScreenHtmlJFrame.getSize();
		if (size.width > parentWidth) {
			size.width = parentWidth;
		}
		if (size.height > parentHeight) {
			size.height = parentHeight;
		}

		brokerScreenHtmlJFrame.setSize(size);
		brokerScreenHtmlJFrame.setLocation(
				(frame.getWidth() - brokerScreenHtmlJFrame.getWidth()) / 2,
				(frame.getHeight() - brokerScreenHtmlJFrame.getHeight()) / 2);
		frame.getLayeredPane().add(brokerScreenHtmlJFrame,
				JLayeredPane.MODAL_LAYER);

		brokerScreenHtmlJFrame.setVisible(true);
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
			logger.warning("Station " + stationNumber + " does not exist!");
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
		if (world.size(KEY.TRAINS, modelRoot.getPrincipal()) > 0) {
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

		dialogueJInternalFrame = new JInternalFrame();
		dialogueJInternalFrame.setFrameIcon(null);
		dialogueJInternalFrame.getContentPane().add(contentPanel);
		dialogueJInternalFrame.pack();

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
		}

		if (null != defaultFocusOwner) {
			defaultFocusOwner.requestFocus();
		}
	}

	public void setDefaultFocusOwner(Component defaultFocusOwner) {
		this.defaultFocusOwner = defaultFocusOwner;
	}

	public void showStationOrTerrainInfo(int x, int y) {
		FreerailsTile tile = (FreerailsTile) world.getTile(x, y);

		TrackRule trackRule = tile.getTrackRule();
		FreerailsPrincipal principal = modelRoot.getPrincipal();
		if (trackRule.isStation()
				&& tile.getOwnerID() == world.getID(principal)) {

			for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
				StationModel station = (StationModel) world.get(KEY.STATIONS,
						i, principal);

				if (null != station && station.x == x && station.y == y) {
					this.showStationInfo(i);

					return;
				}
			}

			throw new IllegalStateException("Couldn't find station at " + x
					+ ", " + y);
		}
		this.showTerrainInfo(x, y);
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