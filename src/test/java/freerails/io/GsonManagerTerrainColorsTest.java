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

package freerails.io;

import freerails.model.terrain.Terrain;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests loading of the overview map terrain colors.
 */
public class GsonManagerTerrainColorsTest extends TestCase {

    /**
     * Tests that the bundled colors name exactly the bundled terrain types.
     *
     * @throws Exception if the resources cannot be loaded
     */
    public void testBundledColorsCoverEveryBundledTerrainType() throws Exception {
        URL terrainTypesUrl = getClass().getResource("/freerails/data/scenario/terrain_types.json");
        assertNotNull(terrainTypesUrl);
        Set<String> terrainNames = GsonManager.loadTerrainTypes(new File(terrainTypesUrl.toURI())).stream()
                .map(Terrain::getName)
                .collect(Collectors.toSet());

        Map<String, ARGBColor> colors;
        try (InputStream in = getClass().getResourceAsStream("/freerails/client/terrain_colors.json")) {
            assertNotNull(in);
            colors = GsonManager.loadTerrainColors(in);
        }

        assertEquals(terrainNames, colors.keySet());
    }

    /**
     * Tests that a color is parsed from its hexadecimal ARGB form.
     *
     * @throws IOException if the colors cannot be read
     */
    public void testParsesHexColorsKeyedByName() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"Clear\": \"#ff00b400\"}".getBytes(StandardCharsets.UTF_8));

        Map<String, ARGBColor> colors = GsonManager.loadTerrainColors(in);

        assertEquals(0xff00b400, colors.get("Clear").getARGB());
    }

    /**
     * Tests that malformed or empty input is reported as an I/O error.
     */
    public void testRejectsMalformedAndEmptyInput() {
        for (String json : new String[]{"{\"Clear\": ", ""}) {
            InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            try {
                GsonManager.loadTerrainColors(in);
                fail("Expected IOException for: " + json);
            } catch (IOException expected) {
                // expected
            }
        }
    }
}
