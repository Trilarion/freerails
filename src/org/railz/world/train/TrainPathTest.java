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

package org.railz.world.train;

import java.awt.Point;

import junit.framework.TestCase;

public class TrainPathTest extends TestCase {
    TrainPath path1;
    TrainPath path2;
    double halfway = (double) (Math.sqrt(2) * 50.0);

    public void setUp() {
	path1 = new TrainPath(new Point[] {new Point (0, 0), new Point (100,
		    100)} );
	path2 = new TrainPath(new Point[] {new Point (100, 100), new Point (200, 200)});
    }

    public void testAppend() {
	Point p = new Point();
	path1.append(path2);
	path1.getHead(p);
	assertTrue (p.x == 0);
	assertTrue (p.y == 0);
	path1.getTail(p);
	assertTrue (p.x == 200);
	assertTrue (p.y == 200);
	assertTrue (path1.getLength() == (int) (4 * halfway));
    }

    public void testTruncateTail() {
	TrainPath truncated = path1.truncateTail((int) halfway + 1);
	Point p = new Point();
	path1.getHead(p);
	assertTrue(p.x == 0);
	assertTrue(p.y == 0);
	path1.getTail(p);
	assertTrue("p.x = " + p.x, p.x == 50);
	assertTrue(p.y == 50);
	assertTrue("path1 = " + path1.toString(), path1.getLength()
		== (int) halfway + 1);
	Point p2 = new Point();
	truncated.getTail(p2);
	assertTrue(p2.x == 100);
	assertTrue(p2.y == 100);
	truncated.getHead(p2);
	assertTrue(p2.x == p.x);
	assertTrue(p2.y == p.y);
    }

    public void testPrepend() {
	Point p = new Point();
	path2.prepend(path1);
	path2.getHead(p);
	assertTrue (p.x == 0);
	assertTrue (p.y == 0);
	path2.getTail(p);
	assertTrue (p.x == 200);
	assertTrue (p.y == 200);
	assertTrue ("path2 = " + path2 + ", halfway * 4 = " + (4 * halfway), path2.getLength() == (int) (4 * halfway));
    }

    public void testMoveHeadTo() {
	path2.moveHeadTo(new TrainPath(path1));
	Point p = new Point();
	Point p2 = new Point();
	path1.getHead(p);
	path2.getHead(p2);
	assertTrue(p.x == p2.x);
	assertTrue(p.y == p2.y);
	path1.getTail(p);
	path2.getTail(p2);
	// not true due to rounding
	// assertTrue("path1 = " + path1 + " path2 = " + path2, p.x == p2.x);
	// assertTrue(p.y == p2.y);
	assertTrue(path1.getLength() == path2.getLength());
    }

    public void testMoveTailTo() {
	path1.moveTailTo(new TrainPath(path2));
	Point p = new Point();
	Point p2 = new Point();
	path1.getHead(p);
	path2.getHead(p2);
	// not true due to rounding
	// assertTrue(p.x == p2.x);
	// assertTrue(p.y == p2.y);
	path1.getTail(p);
	path2.getTail(p2);
	assertTrue(p.x == p2.x);
	assertTrue(p.y == p2.y);
	assertTrue(path1.getLength() == path2.getLength());
    }
}
