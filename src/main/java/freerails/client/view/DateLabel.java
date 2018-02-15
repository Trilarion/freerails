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
import freerails.model.world.WorldItem;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the current date.
 */
public class DateLabel extends JLabel implements View {

    private static final long serialVersionUID = 3689348840578757942L;
    private ReadOnlyWorld world;

    /**
     *
     */
    public DateLabel() {
        setText("          ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != world) {
            GameTime time = world.currentTime();
            GameCalendar gameCalendar = (GameCalendar) world.get(WorldItem.Calendar);
            String s = gameCalendar.getYearAndMonth(time.getTicks());
            super.setText(s);
        }

        super.paintComponent(g);
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
    }
}