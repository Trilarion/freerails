/*
 * BrokerScreenHtmlJFrame.java
 *
 * Created on January 26, 2005, 1:34 PM
 */

package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.net.URL;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.FinancialDataGatherer;
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
    
    /** Creates a new instance of BrokerScreenHtmlJPanel */
    public BrokerScreenHtmlJFrame() {
        super();

        URL url = BrokerScreenHtmlJFrame.class.getResource(
                "/jfreerails/client/view/Broker_Screen.html");
        template = loadText(url);
    }

    public void setup(ModelRoot modelRoot, ViewLists vl,
        ActionListener submitButtonCallBack) {
        super.setup(modelRoot, vl, submitButtonCallBack);
        this.modelRoot = modelRoot;
        updateHtml();
    }
    
    private void updateHtml() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal p = modelRoot.getPrincipal();
        FinancialDataGatherer finacialDataGatherer = new FinancialDataGatherer(world,p);
        brokerScreenGenerator = new BrokerScreenGenerator(world,p);
        
        // this is where the Menu get Enable and Disable by if you own any stock or if the TotalShares are 0
        if(finacialDataGatherer.totalShares() <= 0) {
            disableBuyTreasuryStockJMenuitem();
        }
        else
            enableBuyTreasuryStockJMenuItem();
        if(finacialDataGatherer.treasuryStock() > 0) {
            enableSellTreasuryStockJMenuItem();
        }
        else
            disableSellTreasuryStockJMenuItem();
        
        int numberOfPlayers = modelRoot.getWorld().getNumberOfPlayers();
        for(int i = 0; i < numberOfPlayers; i++) {
           Player temp = modelRoot.getWorld().getPlayer(i);
           finacialDataGatherer = new FinancialDataGatherer(world,temp.getPrincipal());
           if (temp != null && !(modelRoot.getPrincipal().equals(temp.getPrincipal()))) {
               if(finacialDataGatherer.totalShares() <= 0){                 
                 disableBuyPlayerStock(temp);
               }
               else
                   enableBuyPlayerStock(temp);
               if(finacialDataGatherer.otherRRsWithStake().containsKey(world.getID(p))){                 
                 enableSellPlayerStock(temp);
               }
               else
                   disableSellPlayerStock(temp);
           }
        }
        
        // Add any players stock to the Table
        StringBuffer populatedTemplate = new StringBuffer();
        populatedTemplate.append("<html>");
        populatedTemplate.append(populateTokens(template, brokerScreenGenerator));
        
        for(int i = 0; i < world.getNumberOfPlayers();i++){
            if(!(world.getPlayer(i).getPrincipal().equals(p))){
                BrokerScreenGenerator temp = new BrokerScreenGenerator(world, world.getPlayer(i).getPrincipal());
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
        int currentNumberOfTransactions = world.getNumberOfTransactions(playerPrincipal);

        if (currentNumberOfTransactions != lastNumTransactions) {
            updateHtml();
        }

        super.paintComponent(g);
    }
}
