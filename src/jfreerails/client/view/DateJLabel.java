/*
 * Created on 01-Jun-2003
 * 
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;

import javax.swing.JLabel;

import jfreerails.client.model.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;

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
			super.setText(s);
		}
		super.paint(g);
	}

	public void setup(ModelRoot mr) {
		this.w = mr.getWorld();
	}

}
