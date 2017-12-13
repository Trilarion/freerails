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
 * Created on 23-Mar-2003
 * 
 */
package jfreerails.client.view;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * This class implements the GoF Adapter pattern.  It converts the
 * interface of a list on the World to a ListModel interface that can
 * be used by JLists.  Currently, change notification is <b>not</b> implemented.
 * @author Luke
 * 
 */
public class World2ListModelAdapter implements ListModel {
	
	private final KEY k;
	
	private final ReadOnlyWorld w;

	private final FreerailsPrincipal principal;
	
	public World2ListModelAdapter(ReadOnlyWorld world, KEY key,
		FreerailsPrincipal p){
		this.k=key;
		this.w=world;
		principal = p;
	}

	public int getSize() {
		return w.size(k, principal);
	}

	public Object getElementAt(int i) {
		return w.get(k, i, principal);
	}

	public void addListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub

	}

	public void removeListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub

	}

}
