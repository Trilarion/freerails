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

import freerails.io.GsonManager;
import freerails.model.cargo.Cargo;
import freerails.model.game.Clock;
import freerails.model.game.Rules;
import freerails.model.game.Sentiment;
import freerails.model.game.Time;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.util.Array2D;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

/** Reads and writes scenario configuration files outside the world model. */
public final class ScenarioDataLoader {

    private static final String SETTINGS_FILE = "scenario_settings.json";

    private ScenarioDataLoader() {}

    /**
     * Loads a scenario from the established collection of JSON files.
     *
     * @param directory scenario data directory
     * @param mapName normalized map name, without the {@code _map.json} suffix
     */
    public static ScenarioData load(File directory, String mapName) throws IOException {
        File enginesFile = file(directory, "engines.json");
        File citiesFile = file(directory, mapName + "_cities.json");
        File cargosFile = file(directory, "cargo_types.json");
        File terrainTypesFile = file(directory, "terrain_types.json");
        File trackTypesFile = file(directory, "track_types.json");
        File rulesFile = file(directory, "rules.json");
        File sentimentsFile = file(directory, "sentiments.json");
        File mapFile = file(directory, mapName + "_map.json");

        SortedSet<Engine> engines = GsonManager.loadEngines(enginesFile);
        SortedSet<City> cities = GsonManager.loadCities(citiesFile);
        SortedSet<Cargo> cargos = GsonManager.loadCargoTypes(cargosFile);
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(terrainTypesFile);
        SortedSet<TrackType> trackTypes = GsonManager.loadTrackTypes(trackTypesFile);
        Rules rules = GsonManager.load(rulesFile, Rules.class);
        SortedSet<Sentiment> sentiments = GsonManager.loadSentiments(sentimentsFile);
        Array2D map = GsonManager.loadArray2D(mapFile);
        ScenarioSettings settings = loadSettings(directory);

        return new ScenarioData(engines, cities, cargos, terrainTypes, trackTypes, sentiments,
                rules, map, settings.clock(), settings.currentSentimentId(),
                settings.cityGrowthEnabled() == null || settings.cityGrowthEnabled());
    }

    /**
     * Writes scenario data using the established per-definition JSON files.
     * The settings file preserves clock, sentiment, and city-growth selections
     * across a load/write/load cycle.
     */
    public static void save(File directory, String mapName, ScenarioData scenario) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Could not create scenario directory: " + directory);
        }
        GsonManager.save(file(directory, "engines.json"), scenario.getEngines());
        GsonManager.save(file(directory, mapName + "_cities.json"), scenario.getCities());
        GsonManager.save(file(directory, "cargo_types.json"), scenario.getCargos());
        GsonManager.save(file(directory, "terrain_types.json"), scenario.getTerrainTypes());
        GsonManager.save(file(directory, "track_types.json"), scenario.getTrackTypes());
        GsonManager.save(file(directory, "rules.json"), scenario.getRules());
        GsonManager.save(file(directory, "sentiments.json"), scenario.getSentiments());
        GsonManager.saveCompact(file(directory, mapName + "_map.json"), scenario.getTerrainMap());
        GsonManager.save(file(directory, SETTINGS_FILE),
                new ScenarioSettings(scenario.getClock(), scenario.getCurrentSentimentId(),
                        scenario.isCityGrowthEnabled()));
    }

    private static ScenarioSettings loadSettings(File directory) throws IOException {
        File settingsFile = file(directory, SETTINGS_FILE);
        if (!settingsFile.exists()) {
            return new ScenarioSettings(new Clock(1840, 1200, new Time(0)), 2, true);
        }
        return GsonManager.load(settingsFile, ScenarioSettings.class);
    }

    private static File file(File directory, String name) {
        return new File(directory, name);
    }

    private record ScenarioSettings(Clock clock, int currentSentimentId, Boolean cityGrowthEnabled) {
    }
}
