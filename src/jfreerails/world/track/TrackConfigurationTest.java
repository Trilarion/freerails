/*
 * Copyright (C) 2002 Luke Lindsay
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
 * TrackConfigurationTest.java
 * JUnit based test
 *
 *Tests that adding and removing track sections from a configuration.
 * Created on 26 January 2002, 02:25
 */
package jfreerails.world.track;

import jfreerails.world.common.OneTileMoveVector;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author lindsal
 */
public class TrackConfigurationTest extends TestCase {
    public TrackConfigurationTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TrackConfigurationTest.class);

        return suite;
    }

    public void testAdd() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration b = TrackConfiguration.add(a,
                OneTileMoveVector.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("100010000"), b);
        assertEquals(false, a == b);
    }

    public void testSubtract() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("100010000");
        TrackConfiguration b = TrackConfiguration.subtract(a,
                OneTileMoveVector.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("000010000"), b);
    }
}