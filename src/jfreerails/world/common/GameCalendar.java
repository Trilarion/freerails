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

package jfreerails.world.common;

import java.util.GregorianCalendar;

/** 
 * This class stores constants which are properties of the way time is
 * measured in the game world.
 */
final public class GameCalendar implements FreerailsSerializable {
    private final int ticksPerDay;
    private final int startYear;

    public GregorianCalendar getCalendar(GameTime time) {
	GregorianCalendar c = new GregorianCalendar(startYear, 0, 1);
	c.add(GregorianCalendar.HOUR, 24 * time.getTime() / ticksPerDay);
	return c;
    }

    public GameCalendar(int ticksPerDay, int startYear) {
        this.ticksPerDay = ticksPerDay;
        this.startYear = startYear;
    }

    public boolean equals(Object o) {
        if (o instanceof GameCalendar) {
            GameCalendar test = (GameCalendar)o;

            if (this.startYear != test.startYear ||
                    this.ticksPerDay != test.ticksPerDay) {
                return false;
            } else {
                return true;
            }
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
}
