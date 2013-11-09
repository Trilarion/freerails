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
package org.railz.world.common;

/**
 * This class represents a specific instant in time during a game.
 * 
 * @author Luke
 * 
 */
public class GameTime implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 495600254873407658L;

	/**
	 * A BigTick is defined to be this many Ticks. A BigTick is the amount of
	 * time that the server will allow to pass before sending a time
	 * synchronization to the clients. XXX This is not yet implemented as
	 * described.
	 */
	public static final int TICKS_PER_BIG_TICK = 30;

	private final int time;

	@Override
	public String toString() {
		return "GameTime:" + String.valueOf(time);
	}

	public GameTime(int l) {
		this.time = l;
	}

	/**
	 * @return the amount of elapsed game time in Ticks
	 */
	public int getTime() {
		return time;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof GameTime) {
			GameTime test = (GameTime) o;

			return this.time == test.time;
		} else {
			return false;
		}
	}
}
