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
 * LegalTrackConfigurationsTest.java
 * JUnit based test
 *
 *This test checks that the String representation of track configurations that
 *is used in the ruleset are processed correctly by the
 *constructor of LegalTrackConfigurations.
 *
 * Created on 21 January 2002, 23:15
 */
package jfreerails.world.track;

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author lindsal
 */
public class LegalTrackConfigurationsTest extends TestCase {
    public LegalTrackConfigurationsTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(LegalTrackConfigurationsTest.class);
    }

    public void testTrackPieceIsLegal() {
        ArrayList templates = new ArrayList();

        templates.add("000111000");

        LegalTrackConfigurations ltc = new LegalTrackConfigurations(-1,
                templates);

        TrackConfiguration template = TrackConfiguration.getFlatInstance(
                "010010010");
        assertEquals(true, ltc.trackConfigurationIsLegal(template));
        template = TrackConfiguration.getFlatInstance("010111000");
        assertEquals(false, ltc.trackConfigurationIsLegal(template));
    }
}