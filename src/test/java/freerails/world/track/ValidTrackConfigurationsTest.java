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
 * ValidTrackConfigurationsTest.java
 * JUnit based test
 *
 *This test checks that the String representation of track configurations that
 *is used in the ruleset are processed correctly by the
 *constructor of ValidTrackConfigurations.
 *
 */
package freerails.world.track;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;

/**
 *
 */
public class ValidTrackConfigurationsTest extends TestCase {

    /**
     *
     */
    public void testTrackPieceIsLegal() {
        ArrayList<String> templates = new ArrayList<>();
        templates.add("000111000");

        ValidTrackConfigurations validTrackConfigurations = new ValidTrackConfigurations(-1, templates);

        TrackConfiguration template = TrackConfiguration.getFlatInstance("010010010");
        assertEquals(true, validTrackConfigurations.trackConfigurationIsLegal(template));
        template = TrackConfiguration.getFlatInstance("010111000");
        assertFalse(validTrackConfigurations.trackConfigurationIsLegal(template));
    }
}