/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.lang.ref.*;
/**
 * Provides a handy array container for storing weak references, for use with
 * objects which create events.
 */
public class WeakRefList {
    private ArrayList listeners = new ArrayList();

    private class EnumerationImpl implements Enumeration {
	private ArrayList listeners = WeakRefList.this.listeners;
	private int i = 0;
	private Object next;

	public boolean hasMoreElements() {
	    while (i < listeners.size()) {
		/* grab the reference so it doesn't get GC'd when
		 * nextElement() is called */
		next = ((WeakReference) listeners.get(i)).get();
		if (next != null)
		    return true;

		i++;
	    }
	    return false;
	}

	public Object nextElement() {
	    i++;
	    return next;
	}
    }

    private void cleanup() {
	for (int i = 0; i < listeners.size(); i++) {
	    if (((WeakReference) listeners.get(i)).get() == null)
		listeners.remove(i);
	}
    }

    public synchronized void add(Object o) {
	assert o != null;
	listeners = new ArrayList(listeners);
	cleanup();
	listeners.add(new WeakReference(o));
    }

    public synchronized boolean remove(Object o) {
	assert o != null;
	listeners = new ArrayList(listeners);
	cleanup();
	for (int i = 0; i < listeners.size(); i++) {
	    if (o.equals(((WeakReference) listeners.get(i)).get())) {
		listeners.remove(i);
		return true;
	    }
	}
	return false;
    }

    public Enumeration elements() {
	return new EnumerationImpl();
    }
}

