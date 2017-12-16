/*
 * BrokerScreenHtmlJFrame.java
 *
 * Created on January 26, 2005, 1:34 PM
 */

package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.config.ClientConfig;
import freerails.controller.FinancialDataGatherer;
import freerails.controller.ModelRoot;
import freerails.controller.StockPriceCalculator;
import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.world.accounts.BondTransaction;
import freerails.world.accounts.StockTransaction;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.ReadOnlyWorld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
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

    private Action[] buyStock, sellStock;

    /**
     * Creates a new instance of BrokerScreenHtmlJPanel
     */
    public BrokerScreenHtmlJFrame() {
        super();

        URL url = BrokerScreenHtmlJFrame.class
                .getResource(ClientConfig.VIEW_BROKER);
        template = loadText(url);
        this.setSize(550, 300);

    }

    private final Action issueBondAction = new AbstractAction("Issue bond") {
        private static final long serialVersionUID = 1L;

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

    private final Action repayBondAction = new AbstractAction("Repay bond") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent arg0) {

            Move bondTransaction = new AddTransactionMove(modelRoot
                    .getPrincipal(), BondTransaction.repayBond(5));
            modelRoot.doMove(bondTransaction);
        }
    };

    @Override
    public void setup(final ModelRoot modelRoot, RenderersRoot vl,
                      Action closeAction) {
        super.setup(modelRoot, vl, closeAction);
        financialDataGatherer = new FinancialDataGatherer(modelRoot.getWorld(),
                modelRoot.getPrincipal());
        this.modelRoot = modelRoot;

        setupStockMenu();
        updateHtml();
        // Sets up the BrokerScreen and Adds ActionListeners to the Menu
        issueBond.setAction(issueBondAction);
        repayBond.setAction(repayBondAction);
    }

    private void setupStockMenu() {
        stocks.removeAll();
        ReadOnlyWorld world = modelRoot.getWorld();
        int thisPlayerId = world.getID(modelRoot.getPrincipal());
        int numberOfPlayers = world.getNumberOfPlayers();
        buyStock = new Action[numberOfPlayers];
        sellStock = new Action[numberOfPlayers];
        for (int playerId = 0; playerId < numberOfPlayers; playerId++) {
            final boolean isThisPlayer = playerId == thisPlayerId;
            final int otherPlayerId = playerId;
            Player otherPlayer = world.getPlayer(playerId);
            String playerLabel = isThisPlayer ? "Treasury stock" : otherPlayer
                    .getName();
            String buyLabel = "Buy 10,000 shares of " + playerLabel;
            String sellLabel = "Sell 10,000 shares of " + playerLabel;

            buyStock[playerId] = new AbstractAction(buyLabel) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent arg0) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot
                            .getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasuryBuyPrice
                            : stockPrice.buyPrice;
                    StockTransaction t = StockTransaction.buyOrSellStock(
                            otherPlayerId, StockTransaction.STOCK_BUNDLE_SIZE,
                            sharePrice);
                    Move move = new AddTransactionMove(
                            modelRoot.getPrincipal(), t);
                    modelRoot.doMove(move);
                    updateHtml();
                }
            };

            sellStock[playerId] = new AbstractAction(sellLabel) {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent arg0) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot
                            .getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasurySellPrice
                            : stockPrice.sellPrice;
                    StockTransaction t = StockTransaction.buyOrSellStock(
                            otherPlayerId, -StockTransaction.STOCK_BUNDLE_SIZE,
                            sharePrice);
                    Move move = new AddTransactionMove(
                            modelRoot.getPrincipal(), t);
                    modelRoot.doMove(move);
                    updateHtml();
                }
            };
            stocks.add(buyStock[playerId]);
            stocks.add(sellStock[playerId]);
        }
        enableAndDisableActions();
    }

    private void enableAndDisableActions() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal p = modelRoot.getPrincipal();

        FinancialDataGatherer thisDataGatherer = new FinancialDataGatherer(
                world, p);

        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        long highestAffordablePrice = world.getCurrentBalance(p).getAmount()
                / StockTransaction.STOCK_BUNDLE_SIZE;
        // Enable and disable stock actions.
        for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {
            Player temp = modelRoot.getWorld().getPlayer(playerId);
            FreerailsPrincipal otherPrincipal = temp.getPrincipal();
            FinancialDataGatherer otherDataGatherer = new FinancialDataGatherer(
                    world, otherPrincipal);

            // If this RR has stock in other RR, then enable sell stock
            boolean hasStockInRR = thisDataGatherer.getStockInRRs()[playerId] > 0;
            sellStock[playerId].setEnabled(hasStockInRR);

            // If the public own some stock, then enable buy stock.
            boolean isStockAvailable = otherDataGatherer.sharesHeldByPublic() > 0;
            buyStock[playerId].setEnabled(isStockAvailable);

            // Don't let player buy 100% of treasury stock.
            if (otherPrincipal.equals(p)) {
                int treasuryStock = otherDataGatherer.treasuryStock();
                int totalStock = otherDataGatherer.totalShares();
                if (StockTransaction.STOCK_BUNDLE_SIZE + treasuryStock >= totalStock) {
                    buyStock[playerId].setEnabled(false);
                }
            }

            // Don't let the player buy stock if they cannot afford it.
            if (stockPrices[playerId].currentPrice.getAmount() > highestAffordablePrice) {
                buyStock[playerId].setEnabled(false);
            }
        }

        // Enable and diable bond actions.
        int outstandingBonds = thisDataGatherer.getBonds();
        repayBondAction.setEnabled(outstandingBonds > 0);
        issueBondAction.setEnabled(outstandingBonds < 4);

    }

    private void updateHtml() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal p = modelRoot.getPrincipal();

        brokerScreenGenerator = new BrokerScreenGenerator(world, p);

        // this is where the Menu get Enable and Disable by if you own any stock
        // or if the TotalShares are 0

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
        String html = populatedTemplate.toString();
        setHtml(html);
        enableAndDisableActions();
    }

    @Override
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
