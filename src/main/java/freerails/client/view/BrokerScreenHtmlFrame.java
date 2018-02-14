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

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.world.finances.FinancialDataGatherer;
import freerails.controller.ModelRoot;
import freerails.world.finances.StockPriceCalculator;
import freerails.world.finances.StockPrice;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.WorldConstants;
import freerails.world.finances.BondItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.StockItemTransaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 */
public class BrokerScreenHtmlFrame extends BrokerFrame implements View {

    private static final long serialVersionUID = 3257003246252800050L;
    private final String template;
    private ModelRoot modelRoot;

    private final Action repayBondAction = new AbstractAction("Repay bond") {

        private static final long serialVersionUID = 440368637080877578L;

        public void actionPerformed(ActionEvent e) {
            Move bondTransaction = new AddTransactionMove(modelRoot.getPrincipal(), BondItemTransaction.repayBond(5));
            modelRoot.doMove(bondTransaction);
        }
    };
    private FinancialDataGatherer financialDataGatherer;
    private final Action issueBondAction = new AbstractAction("Issue bond") {

        private static final long serialVersionUID = -8074364543650188583L;

        public void actionPerformed(ActionEvent e) {

            if (financialDataGatherer.canIssueBond()) {
                Move bondTransaction = new AddTransactionMove(modelRoot.getPrincipal(), BondItemTransaction.issueBond(financialDataGatherer.nextBondInterestRate()));
                modelRoot.doMove(bondTransaction);
            }
        }
    };
    private Action[] buyStock, sellStock;

    /**
     * Creates a new instance of BrokerScreenHtmlJPanel
     */
    public BrokerScreenHtmlFrame() {
        super();

        URL url = BrokerScreenHtmlFrame.class.getResource(ClientConfig.VIEW_BROKER);
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
        financialDataGatherer = new FinancialDataGatherer(m.getWorld(), m.getPrincipal());
        this.modelRoot = m;

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
            String playerLabel = isThisPlayer ? "Treasury stock" : otherPlayer.getName();
            String buyLabel = "Buy 10,000 shares of " + playerLabel;
            String sellLabel = "Sell 10,000 shares of " + playerLabel;

            buyStock[playerId] = new AbstractAction(buyLabel) {

                private static final long serialVersionUID = -6360550478693971570L;

                public void actionPerformed(ActionEvent e) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot.getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasuryBuyPrice : stockPrice.buyPrice;
                    StockItemTransaction stockItemTransaction = StockItemTransaction.buyOrSellStock(otherPlayerId, WorldConstants.STOCK_BUNDLE_SIZE, sharePrice);
                    Move move = new AddTransactionMove(modelRoot.getPrincipal(), stockItemTransaction);
                    modelRoot.doMove(move);
                    updateHtml();
                }
            };

            sellStock[playerId] = new AbstractAction(sellLabel) {

                private static final long serialVersionUID = 3993755349229011031L;

                public void actionPerformed(ActionEvent e) {
                    StockPrice stockPrice = new StockPriceCalculator(modelRoot.getWorld()).calculate()[otherPlayerId];
                    Money sharePrice = isThisPlayer ? stockPrice.treasurySellPrice : stockPrice.sellPrice;
                    StockItemTransaction stockItemTransaction = StockItemTransaction.buyOrSellStock(otherPlayerId, -WorldConstants.STOCK_BUNDLE_SIZE, sharePrice);
                    Move move = new AddTransactionMove(modelRoot.getPrincipal(), stockItemTransaction);
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
        FreerailsPrincipal principal = modelRoot.getPrincipal();

        FinancialDataGatherer thisDataGatherer = new FinancialDataGatherer(world, principal);

        StockPrice[] stockPrices = new StockPriceCalculator(world).calculate();
        // TODO use Money arithmetic
        long highestAffordablePrice = world.getCurrentBalance(principal).amount / WorldConstants.STOCK_BUNDLE_SIZE;
        // Enable and disable stock actions.
        for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {
            Player temp = modelRoot.getWorld().getPlayer(playerId);
            FreerailsPrincipal otherPrincipal = temp.getPrincipal();
            FinancialDataGatherer otherDataGatherer = new FinancialDataGatherer(world, otherPrincipal);

            // If this RR has stock in other RR, then enable sell stock
            boolean hasStockInRR = thisDataGatherer.getStockInRRs()[playerId] > 0;
            sellStock[playerId].setEnabled(hasStockInRR);

            // If the public own some stock, then enable buy stock.
            boolean isStockAvailable = otherDataGatherer.sharesHeldByPublic() > 0;
            buyStock[playerId].setEnabled(isStockAvailable);

            // Don't let player buy 100% of treasury stock.
            if (otherPrincipal.equals(principal)) {
                int treasuryStock = otherDataGatherer.treasuryStock();
                int totalStock = otherDataGatherer.totalShares();
                if (WorldConstants.STOCK_BUNDLE_SIZE + treasuryStock >= totalStock) {
                    buyStock[playerId].setEnabled(false);
                }
            }

            // Don't let the player buy stock if they cannot afford it.
            // TODO use Money arithmetic
            if (stockPrices[playerId].currentPrice.amount > highestAffordablePrice) {
                buyStock[playerId].setEnabled(false);
            }
        }

        // Enable and disable bond actions.
        int outstandingBonds = thisDataGatherer.getBonds();
        repayBondAction.setEnabled(outstandingBonds > 0);
        issueBondAction.setEnabled(outstandingBonds < 4);
    }

    private void updateHtml() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal principal = modelRoot.getPrincipal();

        BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(world, principal);

        // this is where the Menu get Enable and Disable by if you own any stock
        // or if the TotalShares are 0

        StringBuilder populatedTemplate = new StringBuilder();
        populatedTemplate.append("<html>");
        populatedTemplate.append(populateTokens(template, brokerScreenGenerator));

        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            if (!(world.getPlayer(i).getPrincipal().equals(principal))) {
                BrokerScreenGenerator temp = new BrokerScreenGenerator(world, world.getPlayer(i).getPrincipal());
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
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
        int currentNumberOfTransactions = world.getNumberOfTransactions(playerPrincipal);

        int lastNumTransactions = 0;
        if (currentNumberOfTransactions != lastNumTransactions) {
            updateHtml();
        }

        super.paintComponent(g);
    }
}
