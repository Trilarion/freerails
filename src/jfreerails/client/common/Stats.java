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

package jfreerails.client.common;


/**
 * Statistics gathering class for coarse-level timing
 */
public class Stats {
    int total = 0;
    int n = 0;
    String name;
    long started;
    public static boolean statsAreOn = (System.getProperty(
            "jfreerails.client.common.Stats.statsAreOn") != null);

    public Stats(String name) {
        this.name = name;
    }

    public void enter() {
        if (!statsAreOn) {
            return;
        }

        started = System.currentTimeMillis();
    }

    public void exit() {
        if (!statsAreOn) {
            return;
        }

        n++;
        total += System.currentTimeMillis() - started;

        if (n % 200 == 0) {
            System.out.println("Average time of " + name + ":" + (total / n));
            n = total = 0;
        }
    }
}