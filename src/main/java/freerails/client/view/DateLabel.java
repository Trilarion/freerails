/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.world.UnmodifiableWorld;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * This JLabel shows the current date.
 */
public class DateLabel extends JLabel implements View {

    @Serial
    private static final long serialVersionUID = 3689348840578757942L;
    private UnmodifiableWorld world;

    /**
     * Creates a date label instance.
     */
    public DateLabel() {
        setText("          ");
    }

    /**
     * Paints this component.
     * @param g the g
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (null != world) {
            super.setText(world.getClock().getCurrentDateAsString());
        }

        super.paintComponent(g);
    }

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param rendererRoot the renderer root parameter
     * @param closeAction the action invoked when the panel is closed
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
    }
}