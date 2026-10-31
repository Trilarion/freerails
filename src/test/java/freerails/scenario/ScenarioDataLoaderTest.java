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

package freerails.scenario;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.World;
import freerails.util.Vector2D;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests scenario data loader behavior.
 */
public class ScenarioDataLoaderTest extends TestCase {

    /**
     * Tests scenario data round trips and builds equivalent world configuration behavior.
     * @throws Exception if the test scenario data round trips and builds equivalent world configuration operation fails.
     */
    public void testScenarioDataRoundTripsAndBuildsEquivalentWorldConfiguration() throws Exception {
        ScenarioData original = ScenarioDataLoader.load(scenarioDirectory(), "small_south_america");
        ScenarioData configured = new ScenarioData(
                original.getEngines(),
                original.getCities(),
                original.getCargos(),
                original.getTerrainTypes(),
                original.getTrackTypes(),
                original.getSentiments(),
                original.getRules(),
                original.getTerrainMap(),
                new Clock(1900, 100, new Time(15)),
                4,
                false);
        Path temporaryDirectory = Files.createTempDirectory("freerails-scenario-test");

        try {
            ScenarioDataLoader.save(temporaryDirectory.toFile(), "small_south_america", configured);
            ScenarioData loaded = ScenarioDataLoader.load(temporaryDirectory.toFile(), "small_south_america");

            assertEquals(configured, loaded);
            assertEquals(configured.getRules(), loaded.getRules());
            assertFalse(loaded.isCityGrowthEnabled());

            World world = loaded.toWorldBuilder().build();
            assertFalse(world.isCityGrowthEnabled());
            var terrainMap = loaded.getTerrainMap();
            for (int x = 0; x < terrainMap.getWidth(); x++) {
                for (int y = 0; y < terrainMap.getHeight(); y++) {
                    world.setTile(new Vector2D(x, y), new TerrainTile(terrainMap.get(x, y)));
                }
            }

            assertEquals(configured, ScenarioData.fromWorld(world));

            Path settingsPath = temporaryDirectory.resolve("scenario_settings.json");
            JsonObject legacySettings = JsonParser.parseString(Files.readString(settingsPath)).getAsJsonObject();
            legacySettings.remove("cityGrowthEnabled");
            Files.writeString(settingsPath, legacySettings.toString());
            ScenarioData loadedLegacySettings =
                    ScenarioDataLoader.load(temporaryDirectory.toFile(), "small_south_america");
            assertTrue(loadedLegacySettings.isCityGrowthEnabled());
        } finally {
            FileUtils.deleteDirectory(temporaryDirectory.toFile());
        }
    }

    /**
     * Tests missing scenario settings default city growth to enabled behavior.
     * @throws Exception if the test missing scenario settings default city growth to enabled operation fails.
     */
    public void testMissingScenarioSettingsDefaultCityGrowthToEnabled() throws Exception {
        ScenarioData scenario = ScenarioDataLoader.load(scenarioDirectory(), "small_south_america");

        assertTrue(scenario.isCityGrowthEnabled());
        assertTrue(scenario.toWorldBuilder().build().isCityGrowthEnabled());
    }

    /**
     * Performs scenario directory.
     * @return the result of scenario directory.
     * @throws URISyntaxException if the scenario directory operation fails.
     */
    private static File scenarioDirectory() throws URISyntaxException {
        return new File(ScenarioDataLoaderTest.class
                .getResource("/freerails/data/scenario/")
                .toURI());
    }
}
