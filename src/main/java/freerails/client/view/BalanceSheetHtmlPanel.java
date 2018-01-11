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
import freerails.controller.BalanceSheetGenerator;
import freerails.controller.ModelRoot;
import freerails.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * A HtmlJPanel that displays the balance sheet.
 */
public class BalanceSheetHtmlPanel extends HtmlJPanel implements View {

    private static final long serialVersionUID = 3257009873370886964L;
    private final String template;
    private ModelRoot modelRoot;

    /**
     *
     */
    public BalanceSheetHtmlPanel() {
        super();

        URL url = BalanceSheetHtmlPanel.class.getResource(ClientConfig.VIEW_BALANCE_SHEET);
        template = loadText(url);
    }

    /**
     * @param m
     * @param vl
     * @param closeAction
     */
    @Override
    public void setup(ModelRoot m, RendererRoot vl, Action closeAction) {
        super.setup(m, vl, closeAction);
        this.modelRoot = m;
        updateHtml();
    }

    private void updateHtml() {
        ReadOnlyWorld world = modelRoot.getWorld();
        FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
        BalanceSheetGenerator balanceSheetGenerator = new BalanceSheetGenerator(world, playerPrincipal);
        String populatedTemplate = populateTokens(template, balanceSheetGenerator);
        setHtml(populatedTemplate);
    }

    @Override
    protected void paintComponent(Graphics g) {
        /* Check to see if the text needs updating before painting. */
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