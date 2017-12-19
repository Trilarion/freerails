/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
package freerails.world.track;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;

/**
 * JUnit test.
 *
 */
public class LegalTrackConfigurationsTest extends TestCase {

    /**
     *
     * @param testName
     */
    public LegalTrackConfigurationsTest(java.lang.String testName) {
        super(testName);
    }

    /**
     *
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     *
     * @return
     */
    public static Test suite() {
        return new TestSuite(LegalTrackConfigurationsTest.class);
    }

    /**
     *
     */
    public void testTrackPieceIsLegal() {
        ArrayList<String> templates = new ArrayList<>();

        templates.add("000111000");

        LegalTrackConfigurations ltc = new LegalTrackConfigurations(-1,
                templates);

        TrackConfiguration template = TrackConfiguration
                .getFlatInstance("010010010");
        assertEquals(true, ltc.trackConfigurationIsLegal(template));
        template = TrackConfiguration.getFlatInstance("010111000");
        assertEquals(false, ltc.trackConfigurationIsLegal(template));
    }
}