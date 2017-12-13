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
