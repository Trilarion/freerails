/*
 * Created on Mar 18, 2004
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.net.URL;

import javax.swing.Action;

import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.BalanceSheetGenerator;
import jfreerails.controller.ModelRoot;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * A HtmlJPanel that displays the balance sheet.
 * 
 * @author Luke
 * 
 */
public class BalanceSheetHtmlJPanel extends HtmlJPanel implements View {

	private static final long serialVersionUID = 3257009873370886964L;

	private String template;

	private int lastNumTransactions = 0;

	private ModelRoot modelRoot;

	public BalanceSheetHtmlJPanel() {
		super();

		URL url = BalanceSheetHtmlJPanel.class
				.getResource("/jfreerails/client/view/balance_sheet.htm");
		template = loadText(url);
	}

	public void setup(ModelRoot modelRoot, ViewLists vl,
			Action closeAction) {
		super.setup(modelRoot, vl, closeAction);
		this.modelRoot = modelRoot;
		updateHtml();
	}

	private void updateHtml() {
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
		BalanceSheetGenerator balanceSheetGenerator = new BalanceSheetGenerator(
				world, playerPrincipal);
		String populatedTemplate = populateTokens(template,
				balanceSheetGenerator);
		setHtml(populatedTemplate);
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