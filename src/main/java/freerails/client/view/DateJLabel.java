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
import freerails.world.GameCalendar;
import freerails.world.GameTime;
import freerails.world.ITEM;
import freerails.world.ReadOnlyWorld;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the current date.
 */
public class DateJLabel extends JLabel implements View {
    private static final long serialVersionUID = 3689348840578757942L;

    private ReadOnlyWorld w;

    /**
     *
     */
    public DateJLabel() {
        this.setText("          ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != w) {
            GameTime time = w.currentTime();
            GameCalendar gameCalendar = (GameCalendar) w.get(ITEM.CALENDAR);
            String s = gameCalendar.getYearAndMonth(time.getTicks());
            super.setText(s);
        }

        super.paintComponent(g);
    }

    /**
     * @param model
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot model, RendererRoot vl, Action closeAction) {
        this.w = model.getWorld();
    }
}