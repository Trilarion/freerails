/*
 * Created on Mar 18, 2004
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.client.ClientConfig;
import freerails.controller.BalanceSheetGenerator;
import freerails.controller.ModelRoot;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * A HtmlJPanel that displays the balance sheet.
 *
 * @author Luke
 */
public class BalanceSheetHtmlJPanel extends HtmlJPanel implements View {

    private static final long serialVersionUID = 3257009873370886964L;

    private final String template;

    private ModelRoot modelRoot;

    /**
     *
     */
    public BalanceSheetHtmlJPanel() {
        super();

        URL url = BalanceSheetHtmlJPanel.class
                .getResource(ClientConfig.VIEW_BALANCE_SHEET);
        template = loadText(url);
    }

    /**
     *
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    @Override
    public void setup(ModelRoot modelRoot, RenderersRoot vl, Action closeAction) {
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

    @Override
    protected void paintComponent(Graphics g) {
        /* Check to see if the text needs updating before painting. */
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
        int currentNumberOfTransactions = world
                .getNumberOfTransactions(playerPrincipal);

        int lastNumTransactions = 0;
        if (currentNumberOfTransactions != lastNumTransactions) {
            updateHtml();
        }

        super.paintComponent(g);
    }
}