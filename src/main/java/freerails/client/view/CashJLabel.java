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
 *
 */
package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.world.ReadOnlyWorld;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the amount of cash available.
 */
public class CashJLabel extends JLabel implements View {
    private static final long serialVersionUID = 3257853181542412341L;

    private ReadOnlyWorld w;

    private FreerailsPrincipal principal;

    /**
     *
     */
    public CashJLabel() {
        this.setText("          ");
    }

    /**
     * @param model
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot model, RendererRoot vl, Action closeAction) {
        this.w = model.getWorld();
        principal = model.getPrincipal();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != w) {
            Money m = w.getCurrentBalance(principal);
            String s = m.toString();
            this.setText("$" + s);
        }

        super.paintComponent(g);
    }
}