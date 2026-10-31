/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
 * ValidTrackConfigurationsTest.java
 * JUnit based test
 *
 *This test checks that the String representation of track configurations that
 *is used in the ruleset are processed correctly by the
 *constructor of ValidTrackConfigurations.
 *
 */
package freerails.model.track;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Tests valid track configurations behavior.
 */
public class ValidTrackConfigurationsTest extends TestCase {

    /**
     * Tests track piece is legal behavior.
     */
    public void testTrackPieceIsLegal() {
        ArrayList<String> templates = new ArrayList<>();
        templates.add("000111000");

        // TODO need to test that at a later moment again, will be a test for TrackType
        /*
        ValidTrackConfigurations validTrackConfigurations = new ValidTrackConfigurations( templates);

        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance("010010010");
        assertEquals(true, validTrackConfigurations.trackConfigurationIsLegal(trackConfiguration));

        trackConfiguration = TrackConfiguration.getFlatInstance("010111000");
        assertFalse(validTrackConfigurations.trackConfigurationIsLegal(trackConfiguration));
        */
    }
}