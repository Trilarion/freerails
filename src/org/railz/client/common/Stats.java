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

package org.railz.client.common;

import java.util.*;

/**
 * Statistics gathering class for coarse-level timing
 */
public class Stats {
    private Thread[] threads= new Thread[0];
    private int[] counts= new int[0];

    int total = 0;
    int n = 0;
    String name;
    long started;
    public static boolean statsAreOn = (System.getProperty(
            "org.railz.client.common.Stats.statsAreOn") != null);

    public Stats(String name) {
        this.name = name;
    }

    public void enter() {
        if (!statsAreOn) {
            return;
        }

        started = System.currentTimeMillis();
	Thread t = Thread.currentThread();
	int i;
	for (i = 0; i < threads.length; i++) {
	    if (threads[i] == t) {
		counts[i]++;
		return;
	    }
	}
	if (i == threads.length) {
	    Thread[] tThreads = new Thread[threads.length + 1];
	    int[] tCounts = new int[threads.length + 1];
	    for (i = 0; i < threads.length; i++) {
		tThreads[i] = threads[i];
		tCounts[i] = counts[i];
	    }
	    tThreads[threads.length] = t;
	    tCounts[threads.length] = 1;
	    threads = tThreads;
	    counts = tCounts;
	}
    }

    public void exit() {
        if (!statsAreOn) {
            return;
        }

        n++;
        total += System.currentTimeMillis() - started;

        if (n % 200 == 0) {
            System.out.println("Average time of " + name + ":" + (total / n));
	    for (int i = 0; i < threads.length; i++) {
		System.out.println("Thread " + i + " " + threads[i] + ": " +
			counts[i]);
		counts[i] = 0;
	    }
            n = total = 0;
        }
    }
}
