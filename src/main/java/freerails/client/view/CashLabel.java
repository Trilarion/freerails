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
import freerails.client.ModelRoot;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.finances.Money;
import freerails.model.player.FreerailsPrincipal;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the amount of cash available.
 */
public class CashLabel extends JLabel implements View {

    private static final long serialVersionUID = 3257853181542412341L;
    private ReadOnlyWorld world;
    private FreerailsPrincipal principal;

    /**
     *
     */
    public CashLabel() {
        setText("          ");
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
        principal = modelRoot.getPrincipal();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != world) {
            Money m = world.getCurrentBalance(principal);
            String s = m.toString();
            setText('$' + s);
        }

        super.paintComponent(g);
    }
}