/*
 * Created on Mar 18, 2004
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.net.URL;

import javax.swing.Action;

import jfreerails.client.renderer.RenderersRoot;
import jfreerails.controller.ModelRoot;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * A HtmlJPanel that displays the income statement.
 * 
 * @author Luke
 * 
 */
public class IncomeStatementHtmlJPanel extends HtmlJPanel implements View {
	private static final long serialVersionUID = 3257846588885120057L;

	private String template;

	private int lastNumTransactions = 0;

	private ModelRoot modelRoot;

	public IncomeStatementHtmlJPanel() {
		super();

		URL url = IncomeStatementHtmlJPanel.class
				.getResource("/jfreerails/client/view/income_statement.htm");
		template = loadText(url);
	}

	@Override
	public void setup(ModelRoot modelRoot, RenderersRoot vl,
			Action closeAction) {
		super.setup(modelRoot, vl, closeAction);
		this.modelRoot = modelRoot;
		updateHtml();
	}

	private void updateHtml() {
		ReadOnlyWorld world = modelRoot.getWorld();
		FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
		IncomeStatementGenerator balanceSheetGenerator = new IncomeStatementGenerator(
				world, playerPrincipal);
		String populatedTemplate = populateTokens(template,
				balanceSheetGenerator);
		setHtml(populatedTemplate);
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