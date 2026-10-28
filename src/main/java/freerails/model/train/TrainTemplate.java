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
 *
 */
package freerails.model.train;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * Immutable blueprint specifying an engine type and wagon types for production.
 */
public record TrainTemplate(int engineId, List<Integer> wagonTypes) implements Serializable {

    private static final long serialVersionUID = 3545515106038592057L;

    /**
     * @param engineId   engine type identifier
     * @param wagonTypes wagon type identifiers to produce with the engine
     */
    public TrainTemplate(int engineId, @NotNull List<Integer> wagonTypes) {
        this.engineId = engineId;
        this.wagonTypes = List.copyOf(wagonTypes);
    }

    /**
     * Returns a hash code based on the engine type identifier.
     */
    @Override
    public int hashCode() {
        return engineId;
    }

    /**
     * Compares the engine type and ordered wagon types.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainTemplate productionAtEngineShop)) return false;

        if (engineId != productionAtEngineShop.engineId) return false;
        return wagonTypes.equals(productionAtEngineShop.wagonTypes);
    }

    /**
     * Returns the engine type identifier.
     */
    @Override
    public int engineId() {
        return engineId;
    }

    /**
     * Returns the immutable ordered list of wagon type identifiers.
     */
    @Override
    public List<Integer> wagonTypes() {
        return wagonTypes;
    }

    /**
     * Returns a concise description of this production template.
     */
    @Override
    public String toString() {
        return "engine type: " + engineId + ", with " + wagonTypes.size() + " wagons";
    }
}