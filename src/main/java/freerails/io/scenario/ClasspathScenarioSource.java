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

import freerails.model.scenario.ScenarioData;
import freerails.server.ScenarioSource;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Loads scenario definitions from the built-in classpath resources. The
 * resources are read as streams, so this works both from a directory and from
 * inside a JAR.
 */
public final class ClasspathScenarioSource implements ScenarioSource {

    private static final String[] SCENARIO_NAMES = {"South America", "Small South America"};
    private static final String SCENARIO_RESOURCE_PATH = "freerails/data/scenario/";

    private final ClassLoader classLoader;

    /**
     * Creates a source that reads the scenarios bundled with the application.
     */
    public ClasspathScenarioSource() {
        this(ClasspathScenarioSource.class.getClassLoader());
    }

    /**
     * Creates a source that reads the scenario resources through the specified
     * class loader.
     *
     * @param classLoader the class loader holding the scenario resources
     */
    ClasspathScenarioSource(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * Returns the names of the scenarios available in the built-in resources.
     *
     * @return the available scenario names
     */
    @Override
    public String[] getAvailableScenarioNames() {
        return SCENARIO_NAMES.clone();
    }

    /**
     * Loads a scenario by its displayed name.
     *
     * @param scenarioName the scenario name, for example {@code South America}
     * @return the loaded scenario configuration
     * @throws IOException if the resources cannot be located or loaded
     */
    @Override
    public ScenarioData loadScenario(String scenarioName) throws IOException {
        String mapName = scenarioName.toLowerCase(Locale.ROOT).replace(' ', '_');
        return ScenarioDataLoader.load(
                name -> classLoader.getResourceAsStream(SCENARIO_RESOURCE_PATH + name), mapName);
    }
}
