/*
 * Created on Mar 18, 2004
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.net.URL;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * A HtmlJPanel that displays the balance sheet.
 * @author Luke
 *
 */
public class BalanceSheetHtmlJPanel extends HtmlJPanel implements View {
    private String template;
    private int lastNumTransactions = 0;
    private ModelRoot modelRoot;

    public BalanceSheetHtmlJPanel() {
        super();

        URL url = BalanceSheetHtmlJPanel.class.getResource(
                "/jfreerails/client/view/balance_sheet.htm");
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
        FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
        BalanceSheetGenerator balanceSheetGenerator = new BalanceSheetGenerator(world,
                playerPrincipal);
        String populatedTemplate = populateTokens(template,
                balanceSheetGenerator);
        setHtml(populatedTemplate);
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