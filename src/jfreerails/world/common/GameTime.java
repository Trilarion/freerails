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
package jfreerails.world.common;


/**This class represents a specific instant in time during a game.
 *
 * @author Luke
 *
 */
public class GameTime implements FreerailsSerializable {
    private final int time;

    public String toString() {
        return "GameTime:" + String.valueOf(time);
    }

    public GameTime(int l) {
        this.time = l;
    }

    public int getTime() {
        return time;
    }

    public boolean equals(Object o) {
        if (o instanceof GameTime) {
            GameTime test = (GameTime)o;

            return this.time == test.time;
        } else {
            return false;
        }
    }
}