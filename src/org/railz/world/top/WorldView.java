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

package org.railz.world.top;

import java.awt.*;
import java.io.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.track.*;

/**
 * Represents a "View" of the world from the perspective of a player or
 * server. Delegates real work to WorldImpl, but filters out inappropriate
 * accesses. This class is never read, only written, and shares the same
 * serialVerUID as WorldImpl. Hence a view can be serialized and read in by a
 * client as representing the world in its entirety when it is in fact only a
 * portion.
 */
public class WorldView implements World {
    private WorldImpl world;
    private FreerailsPrincipal viewer;

    public WorldView(WorldImpl w, FreerailsPrincipal viewer) {
	world = w;
	this.viewer = viewer;
    }

    public void set(ITEM item, FreerailsSerializable element,
	    FreerailsPrincipal p) {
	world.set(item, element, p);
    }

    public void set(ITEM item, FreerailsSerializable element) {
	world.set(item, element, Player.AUTHORITATIVE);
    }

    public void set(KEY key, int index, FreerailsSerializable element,
	    FreerailsPrincipal p) {
	if (key.isPrivate && ! viewer.equals(p))
	    return;

	world.set(key, index, element, p);
    }

    public void set(KEY key, int index, FreerailsSerializable element) {
	if (key.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return;
	world.set(key, index, element, Player.AUTHORITATIVE);
    }

    public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal
	    p) {
	if (key.isPrivate && ! viewer.equals(p))
	    return -1;
	return world.add(key, element, p);
    }

    public int add(KEY key, FreerailsSerializable element) {
	if (key.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return -1;
	return world.add(key, element, Player.AUTHORITATIVE);
    }

    public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal p) {
	if (key.isPrivate && ! viewer.equals(p))
	    return null;
	return world.removeLast(key, p);
    }

    public FreerailsSerializable removeLast(KEY key) {
	if (key.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return null;
	return world.removeLast(key, Player.AUTHORITATIVE);
    }

    public void setTile(int x, int y, FreerailsTile tile) {
	setTile(x, y, tile);
    }

    public FreerailsSerializable get(ITEM item) {
	return world.get(item, Player.AUTHORITATIVE);
    }

    public FreerailsSerializable get(ITEM item, FreerailsPrincipal p) {
	return world.get(item, p);
    }

    public FreerailsSerializable get(KEY key, int index) {
	if (key.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return null;
	return world.get(key, index, Player.AUTHORITATIVE);
    }

    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p)
	{
	    if (key.isPrivate && ! viewer.equals(p))
		return null;
	    return world.get(key, index, p);
	}

    public int size(KEY key) {
	if (key.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return 0;
	return world.size(key, Player.AUTHORITATIVE);
    }

    public int size(KEY key, FreerailsPrincipal p) {
	if (key.isPrivate && ! viewer.equals(p))
	    return 0;
	return world.size(key, p);
    }

    public int getMapWidth() {
	return world.getMapWidth();
    }

    public int getMapHeight() {
	return world.getMapHeight();
    }

    public FreerailsTile getTile(int x, int y) {
	return world.getTile(x, y);
    }

    public FreerailsTile getTile(Point p) {
	return world.getTile(p);
    }

    public boolean boundsContain(int x, int y) {
	return world.boundsContain(x, y);
    }

    public boolean boundsContain(KEY k, int index) {
	if (k.isPrivate && ! viewer.equals(Player.AUTHORITATIVE))
	    return false;
	return world.boundsContain(k, index, Player.AUTHORITATIVE);
    }

    public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
	if (k.isPrivate && ! viewer.equals(p))
	    return false;
	return world.boundsContain(k, index, p);
    }

    private Object writeReplace() throws ObjectStreamException {
	return world.getReadOnlyView(viewer);
    }
}
