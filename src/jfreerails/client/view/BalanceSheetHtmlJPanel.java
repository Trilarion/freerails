/*
 * Created on Mar 18, 2004
 */
package jfreerails.client.view;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.net.URL;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
/**
 * @author Luke
 *  
 */
public class BalanceSheetHtmlJPanel extends HtmlJPanel implements View {
	private String template;
	private int lastNumTransactions = 0;
	private ModelRoot modelRoot;
	
	public BalanceSheetHtmlJPanel() {
		super();
		URL url = BalanceSheetHtmlJPanel.class
				.getResource("/jfreerails/client/view/balance_sheet.htm");
		template = loadText(url);	
	}
	public void setup(ModelRoot modelRoot, ActionListener submitButtonCallBack) {
		super.setup(modelRoot, submitButtonCallBack);
		this.modelRoot = modelRoot;
		updateHtml();
	}
	
	private void updateHtml() {
		
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal playerPrincipal = modelRoot.getPlayerPrincipal();
		BalanceSheetGenerator balanceSheetGenerator = new BalanceSheetGenerator(
				world, playerPrincipal);
		String populatedTemplate = populateTokens(template,
				balanceSheetGenerator);
		setHtml(populatedTemplate);
	}
	
	protected void paintComponent(Graphics g) {
		/* Check to see if the text needs updating before painting. */
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal playerPrincipal = modelRoot.getPlayerPrincipal();		
		int currentNumberOfTransactions = world.getNumberOfTransactions(playerPrincipal);
		if(currentNumberOfTransactions != lastNumTransactions){
			updateHtml();
		}	
		super.paintComponent(g);
	}
}