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

import freerails.client.ClientConstants;
import freerails.model.finance.IncomeStatementGenerator;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * A HtmlPanel that displays the income statement.
 */
public class IncomeStatementHtmlPanel extends HtmlPanel implements View {

    private static final long serialVersionUID = 3257846588885120057L;
    private final String template;
    private ModelRoot modelRoot;

    /**
     *
     */
    public IncomeStatementHtmlPanel() {
        super();

        URL url = IncomeStatementHtmlPanel.class.getResource(ClientConstants.VIEW_INCOME_STATEMENT);
        template = loadText(url);
    }

    /**
     * @param m
     * @param rendererRoot
     * @param closeAction
     */
    @Override
    public void setup(ModelRoot m, RendererRoot rendererRoot, Action closeAction) {
        super.setup(m, rendererRoot, closeAction);
        modelRoot = m;
        updateHtml();
    }

    private void updateHtml() {
        UnmodifiableWorld world = modelRoot.getWorld();
        Player playerPlayer = modelRoot.getPlayer();
        IncomeStatementGenerator balanceSheetGenerator = new IncomeStatementGenerator(world, playerPlayer);
        balanceSheetGenerator.calculateAll();
        String populatedTemplate = populateTokens(template, balanceSheetGenerator);
        setHtml(populatedTemplate);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Check to see if the text needs updating before painting.
        UnmodifiableWorld world = modelRoot.getWorld();
        Player player = modelRoot.getPlayer();
        int currentNumberOfTransactions = world.getTransactions(player).size();

        int lastNumTransactions = 0;
        if (currentNumberOfTransactions != lastNumTransactions) {
            updateHtml();
        }

        super.paintComponent(g);
    }
}