/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 01-Jun-2003
 * 
 */
package org.railz.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;

import javax.swing.JLabel;

import org.railz.util.*;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.world.common.GameCalendar;
import org.railz.world.common.GameTime;
import org.railz.world.top.ITEM;
import org.railz.world.top.ReadOnlyWorld;

/**
 * This JLabel shows the current date.
 * @author Luke
 * 
 */
public class DateJLabel extends JLabel {

    private DateFormat dateFormat =
	DateFormat.getDateInstance(DateFormat.MEDIUM);

	private ReadOnlyWorld w;
	
	public DateJLabel(){
		this.setText("          ");	
	}

	public void paint(Graphics g) {
		if (null != w) {
			GameTime time = (GameTime) w.get(ITEM.TIME);
			GameCalendar gameCalendar = (GameCalendar) w.get(ITEM.CALENDAR);
			Calendar c = gameCalendar.getCalendar(time);
				    
			String s = dateFormat.format(c.getTime());

			if (gameCalendar.getTicksPerSecond() == 0)
			    s += " " + Resources.get("PAUSED");

			super.setText(s);
		}
		super.paint(g);
	}

	public void setup(ModelRoot mr) {
		this.w = mr.getWorld();
	}

}
