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

package freerails.io.scenario;

import freerails.io.GsonManager;
import freerails.model.scenario.ScenarioData;
import freerails.model.operations.cargo.Cargo;
import freerails.model.simulation.Clock;
import freerails.model.game.Rules;
import freerails.model.game.Sentiment;
import freerails.model.simulation.Time;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Engine;
import freerails.util.IntArray2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

/** Reads and writes scenario configuration files outside the world model. */
public final class ScenarioDataLoader {

    private static final String SETTINGS_FILE = "scenario_settings.json";

    /** Prevents instantiation of this utility class. */
    private ScenarioDataLoader() {}

    /**
     * Loads a scenario from the established collection of JSON files.
     *
     * @param directory scenario data directory
     * @param mapName normalized map name, without the {@code _map.json} suffix
     */
    public static ScenarioData load(File directory, String mapName) throws IOException {
        return load(name -> {
            File file = file(directory, name);
            return file.isFile() ? new FileInputStream(file) : null;
        }, mapName);
    }

    /**
     * Loads a scenario from the established collection of JSON files, opened
     * by name. This also works for resources inside a JAR, which cannot be
     * read as files.
     *
     * @param opener opens a scenario file by name
     * @param mapName normalized map name, without the {@code _map.json} suffix
     * @return the loaded scenario
     * @throws IOException if a required file is missing or cannot be read
     */
    public static ScenarioData load(ScenarioFileOpener opener, String mapName) throws IOException {
        SortedSet<Engine> engines = loadRequired(opener, "engines.json", GsonManager::loadEngines);
        SortedSet<City> cities = loadRequired(opener, mapName + "_cities.json", GsonManager::loadCities);
        SortedSet<Cargo> cargos = loadRequired(opener, "cargo_types.json", GsonManager::loadCargoTypes);
        SortedSet<Terrain> terrainTypes = loadRequired(opener, "terrain_types.json", GsonManager::loadTerrainTypes);
        SortedSet<TrackType> trackTypes = loadRequired(opener, "track_types.json", GsonManager::loadTrackTypes);
        Rules rules = loadRequired(opener, "rules.json", in -> GsonManager.load(in, Rules.class));
        SortedSet<Sentiment> sentiments = loadRequired(opener, "sentiments.json", GsonManager::loadSentiments);
        IntArray2D map = loadRequired(opener, mapName + "_map.json", GsonManager::loadArray2D);
        ScenarioSettings settings = loadSettings(opener);

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
        GsonManager.save(file(directory, "engines.json"), scenario.engines());
        GsonManager.save(file(directory, mapName + "_cities.json"), scenario.cities());
        GsonManager.save(file(directory, "cargo_types.json"), scenario.cargos());
        GsonManager.save(file(directory, "terrain_types.json"), scenario.terrainTypes());
        GsonManager.save(file(directory, "track_types.json"), scenario.trackTypes());
        GsonManager.save(file(directory, "rules.json"), scenario.rules());
        GsonManager.save(file(directory, "sentiments.json"), scenario.sentiments());
        GsonManager.saveCompact(file(directory, mapName + "_map.json"), scenario.terrainMap());
        GsonManager.save(file(directory, SETTINGS_FILE),
                new ScenarioSettings(scenario.clock(), scenario.currentSentimentId(),
                        scenario.cityGrowthEnabled()));
    }

    /**
     * Loads the optional settings file, or returns the default settings if the
     * scenario has none.
     *
     * @param opener opens a scenario file by name
     * @return the parsed scenario settings
     * @throws IOException if the settings file cannot be read or parsed
     */
    private static ScenarioSettings loadSettings(ScenarioFileOpener opener) throws IOException {
        try (InputStream in = opener.open(SETTINGS_FILE)) {
            if (in == null) {
                return new ScenarioSettings(new Clock(1840, 1200, new Time(0)), 2, true);
            }
            return GsonManager.load(in, ScenarioSettings.class);
        }
    }

    /**
     * Opens a required scenario file, loads it and closes it.
     *
     * @param opener opens a scenario file by name
     * @param name the file name
     * @param loader loads the value from the opened stream
     * @param <E> result type
     * @return the loaded value
     * @throws IOException if the file is missing or cannot be read
     */
    private static <E> E loadRequired(ScenarioFileOpener opener, String name, FileLoader<E> loader) throws IOException {
        try (InputStream in = opener.open(name)) {
            if (in == null) {
                throw new FileNotFoundException("Missing scenario file: " + name);
            }
            return loader.load(in);
        }
    }

    /**
     * Opens a scenario file by name.
     */
    @FunctionalInterface
    public interface ScenarioFileOpener {

        /**
         * Opens the named scenario file.
         *
         * @param name the file name, for example {@code engines.json}
         * @return a stream over the file, or {@code null} if there is no such file
         * @throws IOException if the file exists but cannot be opened
         */
        InputStream open(String name) throws IOException;
    }

    /**
     * Loads a value from an open stream.
     *
     * @param <E> result type
     */
    @FunctionalInterface
    private interface FileLoader<E> {
        E load(InputStream in) throws IOException;
    }

    /**
     * Creates a file reference for a named file in a scenario directory.
     *
     * @param directory the scenario directory
     * @param name the file name within that directory
     * @return the referenced file
     */
    private static File file(File directory, String name) {
        return new File(directory, name);
    }

    /**
     * Stores optional clock, sentiment, and city-growth settings from a scenario file.
     *
     * @param clock the optional simulation clock settings
     * @param currentSentimentId the identifier of the current public sentiment
     * @param cityGrowthEnabled whether city growth is enabled, or {@code null} if unspecified
     */
    private record ScenarioSettings(Clock clock, int currentSentimentId, Boolean cityGrowthEnabled) {
    }
}
