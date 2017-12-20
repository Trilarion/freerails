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

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * A HtmlJPanel that displays the income statement.
 */
public class IncomeStatementHtmlJPanel extends HtmlJPanel implements View {
    private static final long serialVersionUID = 3257846588885120057L;

    private final String template;

    private ModelRoot modelRoot;

    /**
     *
     */
    public IncomeStatementHtmlJPanel() {
        super();

        URL url = IncomeStatementHtmlJPanel.class
                .getResource(ClientConfig.VIEW_INCOME_STATEMENT);
        template = loadText(url);
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        super.setup(modelRoot, vl, closeAction);
        this.modelRoot = modelRoot;
        updateHtml();
    }

    private void updateHtml() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
        IncomeStatementGenerator balanceSheetGenerator = new IncomeStatementGenerator(
                world, playerPrincipal);
        balanceSheetGenerator.calculateAll();
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