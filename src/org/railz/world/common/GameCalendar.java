/*
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

package org.railz.world.common;

import java.io.*;
import java.util.GregorianCalendar;

/** 
 * This class stores constants which are properties of the way time is
 * measured in the game world.
 */
final public class GameCalendar implements FreerailsSerializable {
    private static final long serialVersionUID = -2882743313864450614L;

    private final int ticksPerDay;
    private final int startYear;
    private GregorianCalendar t0;
    
    /** number of ticks per real-world second */
    private int ticksPerSecond;

    public int hashCode() {
	return ticksPerDay ^ startYear;
    }

    public GregorianCalendar getCalendar(GameTime time) {
	GregorianCalendar c = new GregorianCalendar(startYear, 0, 1);
	c.add(GregorianCalendar.HOUR, 24 * time.getTime() / ticksPerDay);
	return c;
    }

    public GameTime getTimeFromCalendar(GregorianCalendar c) {
	long deltaT = c.getTimeInMillis() - t0.getTimeInMillis();
	return new GameTime((int) (deltaT /
		    (1000 * 60 * 60 * 24 / ticksPerDay)));
    }

    public GameCalendar(GameCalendar gc, int ticksPerSecond) {
	this(gc.ticksPerDay, gc.startYear, gc.t0, ticksPerSecond);
    }

    public GameCalendar(int ticksPerDay, int startYear, int ticksPerSecond) {
        this(ticksPerDay, startYear, new GregorianCalendar(startYear, 0, 1),
		ticksPerSecond);
    }

    private GameCalendar(int ticksPerDay, int startYear, GregorianCalendar t0,
	    int ticksPerSecond) {
	this.ticksPerDay = ticksPerDay;
	this.startYear = startYear;
	this.t0 = t0;
	this.ticksPerSecond = ticksPerSecond;
    }

    public boolean equals(Object o) {
        if (o instanceof GameCalendar) {
            GameCalendar test = (GameCalendar)o;

            return startYear == test.startYear &&
                    ticksPerDay == test.ticksPerDay &&
		    ticksPerSecond == test.ticksPerSecond &&
		    t0.equals(test.t0);
        } else {
            return false;
        }
    }

    public int getTicksPerDay() {
	return ticksPerDay;
    }

    public int getStartYear() {
	return startYear;
    }

    private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
	in.defaultReadObject();
	if (t0 == null)
	    t0 = new GregorianCalendar(startYear, 0, 1);
    }

    public int getTicksPerSecond() {
	return ticksPerSecond;
    }
}
