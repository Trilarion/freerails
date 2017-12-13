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
package org.railz.client.view;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.util.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.*;

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
	
	private final WeakRefList listeners = new WeakRefList();

	private WorldListListener worldListener = new WorldListListener() {
	    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
		if (key != k)
		    return;

		if (!p.equals(principal))
		    return;

		Enumeration en = listeners.elements();
		ListDataEvent lde = new
		    ListDataEvent(World2ListModelAdapter.this,
			    ListDataEvent.CONTENTS_CHANGED, index, index);
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .contentsChanged(lde);
		    }
		}
	    }

	    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
		if (key != k)
		    return;

		if (!p.equals(principal))
		    return;

		Enumeration en = listeners.elements();
		ListDataEvent lde = new 
		    ListDataEvent(World2ListModelAdapter.this,
			    ListDataEvent.INTERVAL_ADDED, index, index);
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .intervalAdded(lde);
		    }
		}
	    }

	    public void itemRemoved(KEY key, int index, FreerailsPrincipal
		    p) {
		if (key != k)
		    return;

		if (!p.equals(principal))
		    return;

		Enumeration en = listeners.elements();
		ListDataEvent lde = new
		    ListDataEvent(World2ListModelAdapter.this,
			    ListDataEvent.INTERVAL_REMOVED, index, index);
		synchronized (listeners) {
		    while (en.hasMoreElements()) {
			((ListDataListener) en.nextElement())
			    .intervalRemoved(lde);
		    }
		}
	    }
	};

	public World2ListModelAdapter(ReadOnlyWorld world, KEY key,
		FreerailsPrincipal p){
	    this(world, key, p, null);
	}

	public World2ListModelAdapter(ReadOnlyWorld world, KEY key,
		FreerailsPrincipal p, MoveChainFork mcf){
	    this.k=key;
	    this.w=world;
	    principal = p;
	    if (mcf != null)
		mcf.addListListener(worldListener);
	}

	public int getSize() {
		return w.size(k, principal);
	}

	public Object getElementAt(int i) {
		return w.get(k, i, principal);
	}

	public void addListDataListener(ListDataListener l) {
	    System.out.println("listener added");
	    synchronized (listeners) {
		listeners.add(l);
	    }
	}

	public void removeListDataListener(ListDataListener l) {
	    synchronized (listeners) {
		listeners.remove(l);
	    }
	}
}
