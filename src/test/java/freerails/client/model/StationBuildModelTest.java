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

package freerails.client.model;

import freerails.client.ModelRootImpl;
import freerails.controller.StationBuilder;
import freerails.model.MapFixtureFactory2;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.Status;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

/** Tests toolkit-independent station selection and placement state. */
public class StationBuildModelTest extends TestCase {

    private StationBuildModel stationBuildModel;
    private World world;

    /** Creates the build model with the standard world fixture. */
    @Override
    protected void setUp() {
        world = MapFixtureFactory2.getCopy();
        ModelRootImpl modelRoot = new ModelRootImpl();
        modelRoot.setup(world, world.getPlayer(0));
        stationBuildModel = new StationBuildModel(new StationBuilder(modelRoot), modelRoot);
    }

    /** Requires a valid station type before enabling placement. */
    public void testSelectingAndCancellingStationType() {
        assertFalse(stationBuildModel.isStationTypeSelected());

        int stationTypeId = getStationTypeId();
        stationBuildModel.chooseStationType(stationTypeId);
        assertTrue(stationBuildModel.isStationTypeSelected());

        stationBuildModel.cancelStationPlacement();
        assertFalse(stationBuildModel.isStationTypeSelected());
    }

    /** Rejects a track type that is not a station. */
    public void testRejectsUnknownStationType() {
        try {
            stationBuildModel.chooseStationType(-1);
            fail("Expected an unknown station type to be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals("Unknown station track type: -1", expected.getMessage());
        }
    }

    /** Reports a failed build when no station type has been selected. */
    public void testBuildRequiresSelectedStationType() {
        Status status = stationBuildModel.buildStationAt(new IntPoint2D(0, 0));
        assertFalse(status.isSuccess());
        assertEquals("Select a station type before building a station.", status.getMessage());
    }

    /** Returns the first station track-type identifier in the fixture. */
    private int getStationTypeId() {
        for (TrackType trackType : world.getTrackTypes()) {
            if (trackType.isStation()) {
                return trackType.getId();
            }
        }
        throw new AssertionError("Fixture has no station track type.");
    }

}
