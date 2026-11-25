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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.renderer.track;

import freerails.client.ClientConstants;
import freerails.io.GsonManager;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackType;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

/**
 * Tests track asset filename generation.
 */
public class TrackPieceRendererImplTest extends TestCase {

    /**
     * Tests directional masks map to the existing low-bit-first asset names.
     */
    public void testGenerateFilenameUsesDirectionalMask() {
        assertEquals("track" + File.separator + "standard_track_10000000.png",
                TrackPieceRendererImpl.generateFilename(1, "standard_track"));
        assertEquals("track" + File.separator + "standard_track_00000001.png",
                TrackPieceRendererImpl.generateFilename(128, "standard_track"));
    }

    /**
     * Tests that every valid configuration of every bundled track type has an
     * image, so missing graphics are found before a game is started.
     *
     * @throws Exception if the track types cannot be loaded
     */
    public void testBundledTrackTypesHaveImageForEveryValidConfiguration() throws Exception {
        URL trackTypesUrl = getClass().getResource("/freerails/data/scenario/track_types.json");
        assertNotNull(trackTypesUrl);

        for (TrackType trackType : GsonManager.loadTrackTypes(new File(trackTypesUrl.toURI()))) {
            assertFalse(trackType.getValidTrackConfigurations().isEmpty());
            for (TrackConfiguration configuration : trackType.getValidTrackConfigurations()) {
                String fileName = TrackPieceRendererImpl.generateFilename(
                        configuration.getConfiguration(), trackType.getName());
                // Same normalization as ImageManagerImpl: spaces become underscores.
                String resource = (ClientConstants.GRAPHICS_PATH + fileName)
                        .replace(File.separatorChar, '/').replace(' ', '_');
                assertNotNull("Missing track image " + resource, getClass().getResource(resource));
            }
        }
    }
}
