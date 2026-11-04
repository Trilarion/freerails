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

package freerails.server;

import freerails.model.scenario.ScenarioData;

import java.io.IOException;

/** Supplies scenario definitions to the authoritative server. */
public interface ScenarioSource {

    /**
     * Returns the names of scenarios available for a new game.
     *
     * @return the available scenario names
     */
    String[] getAvailableScenarioNames();

    /**
     * Loads a scenario by its displayed name.
     *
     * @param scenarioName the scenario name
     * @return the loaded scenario configuration
     * @throws IOException if the scenario cannot be loaded
     */
    ScenarioData loadScenario(String scenarioName) throws IOException;
}
