/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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

/*
 * BrokerScreenHtmlFrame.java
 *
 */

package freerails.client.view;

import freerails.client.ClientConstants;
import freerails.client.renderer.RendererRoot;
import freerails.model.finance.*;
import freerails.client.ModelRoot;
import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.aggregator.FinancialDataAggregator;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.ModelConstants;
import freerails.model.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

// TODO simplify this code, creates too many FinancialDataAggregator instances
/**
 */
public class BrokerScreenHtmlFrame extends BrokerFrame implements View {

    private static final long serialVersionUID = 3257003246252800050L;
    private final String template;
    private ModelRoot modelRoot;

    private final Action repayBondAction = new AbstractAction("Repay bond") {

        private static final long serialVersionUID = 440368637080877578L;

        @Override
        public void actionPerformed(ActionEvent e) {
            Move bondTransaction = new AddTransactionMove(modelRoot.getPlayer(), TransactionUtils.repayBond(5, modelRoot.getWorld().getClock().getCurrentTime()));
            modelRoot.applyMove(bondTransaction);
        }
    };
    private FinancialDataAggregator financialDataAggregator;
    private final Action issueBondAction = new AbstractAction("Issue bond") {

        private static final long serialVersionUID = -8074364543650188583L;

        @Override
        public void actionPerformed(ActionEvent e) {

            if (financialDataAggregator.canIssueBond()) {
                Move bondTransaction = new AddTransactionMove(modelRoot.getPlayer(), TransactionUtils.issueBond(financialDataAggregator.nextBondInterestRate(), modelRoot.getWorld().getClock().getCurrentTime()));
                modelRoot.applyMove(bondTransaction);
            }
        }
    };
    // TODO should be maps, not arrays
    private Action[] buyStock, sellStock;

    /**
     * Creates a new instance of BrokerScreenHtmlJPanel
     */
    public BrokerScreenHtmlFrame() {
        super();

        URL url = BrokerScreenHtmlFrame.class.getResource(ClientConstants.VIEW_BROKER);
        template = loadText(url);
        setSize(550, 300);
    }

    /**
     * @param m
     * @param rendererRoot
     * @param closeAction
     */
    @Override
    public void setup(final ModelRoot m, RendererRoot rendererRoot, Action closeAction) {
        super.setup(m, rendererRoot, closeAction);
        financialDataAggregator = new FinancialDataAggregator(m.getWorld(), m.getPlayer());
        modelRoot = m;

        setupStockMenu();
        updateHtml();
        // Sets up the BrokerScreen and Adds ActionListeners to the Menu
        issueBond.setAction(issueBondAction);
        repayBond.setAction(repayBondAction);
    }

    private void setupStockMenu() {
        stocks.removeAll();
        UnmodifiableWorld world = modelRoot.getWorld();
        int thisPlayerId = modelRoot.getPlayer().getId();
        int numberOfPlayers = world.getPlayers().size();
        buyStock = new Action[numberOfPlayers];
        sellStock = new Action[numberOfPlayers];
        for (int playerId = 0; playerId < numberOfPlayers; playerId++) {
            final boolean isThisPlayer = playerId == thisPlayerId;
            final int otherPlayerId = playerId;
            Player otherPlayer = world.getPlayer(playerId);
            String playerLabel = isThisPlayer ? "Treasury stock" : otherPlayer.getName();
            String buyLabel = "Buy 10,000 shares of " + playerLabel;
            String sellLabel = "Sell 10,000 shares of " + playerLabel;

            buyStock[playerId] = new AbstractAction(buyLabel) {

                private static final long serialVersionUID = -6360550478693971570L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot.getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasuryBuyPrice : stockPrice.buyPrice;
                    ItemTransaction stockItemTransaction = TransactionUtils.buyOrSellStock(otherPlayerId, ModelConstants.STOCK_BUNDLE_SIZE, sharePrice, modelRoot.getWorld().getClock().getCurrentTime());
                    Move move = new AddTransactionMove(modelRoot.getPlayer(), stockItemTransaction);
                    modelRoot.applyMove(move);
                    updateHtml();
                }
            };

            sellStock[playerId] = new AbstractAction(sellLabel) {

                private static final long serialVersionUID = 3993755349229011031L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot.getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasurySellPrice : stockPrice.sellPrice;
                    ItemTransaction stockItemTransaction = TransactionUtils.buyOrSellStock(otherPlayerId, -ModelConstants.STOCK_BUNDLE_SIZE, sharePrice, modelRoot.getWorld().getClock().getCurrentTime());
                    Move move = new AddTransactionMove(modelRoot.getPlayer(), stockItemTransaction);
                    modelRoot.applyMove(move);
                    updateHtml();
                }
            };
            stocks.add(buyStock[playerId]);
            stocks.add(sellStock[playerId]);
        }
        enableAndDisableActions();
    }

    private void enableAndDisableActions() {
        UnmodifiableWorld world = modelRoot.getWorld();
        Player player = modelRoot.getPlayer();

        FinancialDataAggregator thisDataGatherer = new FinancialDataAggregator(world, player);

        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        // TODO use Money arithmetic
        long highestAffordablePrice = world.getCurrentBalance(player).amount / ModelConstants.STOCK_BUNDLE_SIZE;
        // Enable and disable stock actions.
        for (Player otherPlayer: world.getPlayers()) {
            FinancialDataAggregator otherDataGatherer = new FinancialDataAggregator(world, otherPlayer);

            // If this RR has stock in other RR, then enable sell stock
            boolean hasStockInRR = thisDataGatherer.getStockInRRs()[otherPlayer.getId()] > 0;
            sellStock[otherPlayer.getId()].setEnabled(hasStockInRR);

            // If the public own some stock, then enable buy stock.
            boolean isStockAvailable = otherDataGatherer.sharesHeldByPublic() > 0;
            buyStock[otherPlayer.getId()].setEnabled(isStockAvailable);

            // Don't let player buy 100% of treasury stock.
            if (otherPlayer.equals(player)) {
                int treasuryStock = otherDataGatherer.treasuryStock();
                int totalStock = otherDataGatherer.totalShares();
                if (ModelConstants.STOCK_BUNDLE_SIZE + treasuryStock >= totalStock) {
                    buyStock[otherPlayer.getId()].setEnabled(false);
                }
            }

            // Don't let the player buy stock if they cannot afford it.
            // TODO use Money arithmetic
            if (stockPrices[otherPlayer.getId()].currentPrice.amount > highestAffordablePrice) {
                buyStock[otherPlayer.getId()].setEnabled(false);
            }
        }

        // Enable and disable bond actions.
        int outstandingBonds = thisDataGatherer.getBonds();
        repayBondAction.setEnabled(outstandingBonds > 0);
        issueBondAction.setEnabled(outstandingBonds < 4);
    }

    private void updateHtml() {
        UnmodifiableWorld world = modelRoot.getWorld();
        Player player = modelRoot.getPlayer();

        BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(world, player);

        // this is where the Menu get Enable and Disable by if you own any stock or if the TotalShares are 0

        StringBuilder populatedTemplate = new StringBuilder();
        populatedTemplate.append("<html>");
        populatedTemplate.append(populateTokens(template, brokerScreenGenerator));

        for (Player otherPlayer: world.getPlayers()) {
            if (!(otherPlayer.equals(player))) {
                BrokerScreenGenerator temp = new BrokerScreenGenerator(world, otherPlayer);
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
        // Check to see if the text needs updating before painting.
        UnmodifiableWorld world = modelRoot.getWorld();
        Player playerPlayer = modelRoot.getPlayer();
        int currentNumberOfTransactions = world.getTransactions(playerPlayer).size();

        int lastNumTransactions = 0;
        if (currentNumberOfTransactions != lastNumTransactions) {
            updateHtml();
        }

        super.paintComponent(g);
    }
}
