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
import jfreerails.world.accounts.StockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * 
 * @author smackay
 * @author Luke
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

			FreerailsPrincipal principal = modelRoot
					.getPrincipal();
			
			Money sharePrice = financialDataGatherer.sharePrice();
			int playerId = modelRoot.getWorld().getID(modelRoot.getPrincipal());
			Transaction t = StockTransaction.buyOrSellStock(playerId, 10000, sharePrice);
			Move StockTransaction = new AddTransactionMove(principal, t);
			modelRoot.doMove(StockTransaction);
		}
	};

	private final ActionListener sellTreasuryStockActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			int playerId = modelRoot.getWorld().getID(modelRoot.getPrincipal());
			Move move = new AddTransactionMove(modelRoot
					.getPrincipal(), StockTransaction.buyOrSellStock(playerId, -10000,
					financialDataGatherer.sharePrice()));
			modelRoot.doMove(move);
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

		//Sets up Actions listeners for buy/sell players' stock
		int numberOfPlayers = modelRoot.getWorld().getNumberOfPlayers();
		for (int i = 0; i < numberOfPlayers; i++) {
			final Player otherPlayer = modelRoot.getWorld().getPlayer(i);
			final int otherPlayerId = i;
			final FreerailsPrincipal principal = modelRoot.getPrincipal();			
			final FinancialDataGatherer otherPlayersData = new FinancialDataGatherer(
					modelRoot.getWorld(), otherPlayer.getPrincipal());
			if (otherPlayer != null
					&& !(principal.equals(otherPlayer.getPrincipal()))) {
				this.enableBuyPlayerStock(otherPlayer);
				this.enableSellPlayerStock(otherPlayer);
			}

			ActionListener buyStockAl = new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							Money sharePrice = otherPlayersData.sharePrice();
							StockTransaction issueStock = StockTransaction.buyOrSellStock(otherPlayerId,
											10000, sharePrice);
							Move move = new AddTransactionMove(
									principal,
									issueStock);
							modelRoot.doMove(move);													
						}
					};
			this.setBuyPlayerStockActionlistener(					
					buyStockAl, otherPlayer);
			
			ActionListener sellStockAl = new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							Money sharePrice = otherPlayersData.sharePrice();
							StockTransaction sellStock = StockTransaction.buyOrSellStock(otherPlayerId,
											-10000, sharePrice);
							Move move = new AddTransactionMove(
									principal,
									sellStock);
							modelRoot.doMove(move);							
						}
					};
			this.setSellPlayerStockActionlistener(
					sellStockAl, otherPlayer);
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
		if (finacialDataGatherer.sharesHeldByPublic() <= 0) {
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
				if (finacialDataGatherer.thisRRHasStakeIn(i)) {
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
