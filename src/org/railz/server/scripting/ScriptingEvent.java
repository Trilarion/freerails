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
package org.railz.server.scripting;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.move.Move;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.GameTime;
import org.railz.world.top.ReadOnlyWorld;

/**
 * Defines an interface implemented by a Scripting Event. All concrete classes
 * of ScriptingEvent must have a constructor of the form ClassName(ReadOnlyWorld
 * w, GameTime startTime, GameTime endTime, Map params)
 */
public abstract class ScriptingEvent implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4839492777012779949L;
	protected GameTime startTime;
	protected GameTime endTime;
	private static final String CLASS_NAME = ScriptingEvent.class.getName();
	private static final Logger logger = LogManager.getLogger(CLASS_NAME);

	/** @return the scheduled start time of the event */
	public GameTime getStartTime() {
		return startTime;
	}

	/** @return the scheduled end time of the event */
	public GameTime getEndTime() {
		return endTime;
	}

	protected ScriptingEvent(GameTime startTime, GameTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/** @return a Move to be executed when the event occurs */
	public abstract Move getMove(ReadOnlyWorld w);

	/** Factory method for creating events */
	public static ScriptingEvent createEvent(String className, ReadOnlyWorld w,
			GameTime startTime, GameTime endTime, Map attributes) {
		try {
			Class seClass = ScriptingEvent.class.getClassLoader().loadClass(
					"org.railz.server.scripting." + className);
			Constructor seConstructor = seClass.getConstructor(new Class[] {
					ReadOnlyWorld.class, GameTime.class, GameTime.class,
					Map.class });
			return (ScriptingEvent) seConstructor.newInstance(new Object[] { w,
					startTime, endTime, attributes });
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"ScriptingEvent caught " + e.getMessage(), e);
			return null;
		}
	}
}
