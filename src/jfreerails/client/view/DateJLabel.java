/*
 * Created on 01-Jun-2003
 * 
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.World;

/**
 * This JLabel shows the current date.
 * @author Luke
 * 
 */
public class DateJLabel extends JLabel implements View {

	private World w;
	
	public DateJLabel(){
		this.setText("DATE NOT SET!");	
	}

	public void paint(Graphics g) {
		if (null != w) {
			GameTime time = (GameTime) w.get(ITEM.TIME);
			GameCalendar gameCalendar = (GameCalendar) w.get(ITEM.CALENDAR);
			String s = gameCalendar.getYearAndMonth(time.getTime());
			super.setText(s);
		}
		super.paint(g);
	}

	public void setup(World w, ViewLists vl, ActionListener submitButtonCallBack) {
		this.w = w;
	}

}
