/*
 * BrokerScreenHtmlJFrame.java
 *
 * Created on January 26, 2005, 1:34 PM
 */

package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.FinancialDataGatherer;
import jfreerails.controller.ModelRoot;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.IssueStockTransaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * 
 * @author smackay
 */
public class BrokerScreenHtmlJFrame extends BrokerJFrame implements View {

	private static final long serialVersionUID = 3257003246252800050L;

	private String template;

	private int lastNumTransactions = 0;

	private ModelRoot modelRoot;

	public static BrokerScreenGenerator brokerScreenGenerator;
	
	private FinancialDataGatherer financialDataGatherer;

	/** Creates a new instance of BrokerScreenHtmlJPanel */
	public BrokerScreenHtmlJFrame() {
		super();

		URL url = BrokerScreenHtmlJFrame.class
				.getResource("/jfreerails/client/view/Broker_Screen.html");
		template = loadText(url);
	}
	
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

	public void setup(final ModelRoot modelRoot, ViewLists vl,
			ActionListener submitButtonCallBack) {
		super.setup(modelRoot, vl, submitButtonCallBack);
		financialDataGatherer = new FinancialDataGatherer(
				modelRoot.getWorld(), modelRoot.getPrincipal());
		this.modelRoot = modelRoot;
		updateHtml();
//		 Sets up the BrokerScreen and Adds ActionListeners to the Menu
		this
				.setIssueBondActionListener(this.issueBondActionListener);
		this
				.setRepayBondActionListener(this.repayBondActionListener);
		this
				.setBuytreasuryStockActionListener(this.buyTreasuryStockActionListener);
		this
				.setSellTreasuryStockActionlistener(this.sellTreasuryStockActionListener);

		// for every player this is seting up an ActionListener to Buy and Sell
		// there stock
		int numberOfPlayers = modelRoot.getWorld().getNumberOfPlayers();
		for (int i = 0; i < numberOfPlayers; i++) {
			final Player temp = modelRoot.getWorld().getPlayer(i);
			@SuppressWarnings("unused")
			FinancialDataGatherer data4tempPlayer = new FinancialDataGatherer(
					modelRoot.getWorld(), temp.getPrincipal());
			if (temp != null
					&& !(modelRoot.getPrincipal().equals(temp.getPrincipal()))) {
				this.enableBuyPlayerStock(temp);
				this.enableSellPlayerStock(temp);
			}

			this.setBuyPlayerStockActionlistener(
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
			this.setSellPlayerStockActionlistener(
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

	private void updateHtml() {
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal p = modelRoot.getPrincipal();
		FinancialDataGatherer finacialDataGatherer = new FinancialDataGatherer(
				world, p);
		brokerScreenGenerator = new BrokerScreenGenerator(world, p);

		// this is where the Menu get Enable and Disable by if you own any stock
		// or if the TotalShares are 0
		if (finacialDataGatherer.totalShares() <= 0) {
			disableBuyTreasuryStockJMenuitem();
		} else
			enableBuyTreasuryStockJMenuItem();
		if (finacialDataGatherer.treasuryStock() > 0) {
			enableSellTreasuryStockJMenuItem();
		} else
			disableSellTreasuryStockJMenuItem();

		int numberOfPlayers = modelRoot.getWorld().getNumberOfPlayers();
		for (int i = 0; i < numberOfPlayers; i++) {
			Player temp = modelRoot.getWorld().getPlayer(i);
			finacialDataGatherer = new FinancialDataGatherer(world, temp
					.getPrincipal());
			if (temp != null
					&& !(modelRoot.getPrincipal().equals(temp.getPrincipal()))) {
				if (finacialDataGatherer.totalShares() <= 0) {
					disableBuyPlayerStock(temp);
				} else
					enableBuyPlayerStock(temp);
				if (finacialDataGatherer.otherRRsWithStake().containsKey(
						world.getID(p))) {
					enableSellPlayerStock(temp);
				} else
					disableSellPlayerStock(temp);
			}
		}

		// Add any players stock to the Table
		StringBuffer populatedTemplate = new StringBuffer();
		populatedTemplate.append("<html>");
		populatedTemplate
				.append(populateTokens(template, brokerScreenGenerator));

		for (int i = 0; i < world.getNumberOfPlayers(); i++) {
			if (!(world.getPlayer(i).getPrincipal().equals(p))) {
				BrokerScreenGenerator temp = new BrokerScreenGenerator(world,
						world.getPlayer(i).getPrincipal());
				populatedTemplate.append(populateTokens(template, temp));
			}
		}
		populatedTemplate.append("</html>");
		setHtml(populatedTemplate.toString());
	}

	protected void paintComponent(Graphics g) {
		/* Check to see if the text needs updating before painting. */
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
		int currentNumberOfTransactions = world
				.getNumberOfTransactions(playerPrincipal);

		if (currentNumberOfTransactions != lastNumTransactions) {
			updateHtml();
		}

		super.paintComponent(g);
	}
}
