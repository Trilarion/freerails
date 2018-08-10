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
 *
 */
package freerails.model.train;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the blue print for what an engine shop is producing.
 */
public class TrainTemplate implements Serializable {

    private static final long serialVersionUID = 3545515106038592057L;
    private final int engineId;
    private final List<Integer> wagonTypes;

    /**
     * @param engineId
     * @param wagonTypes
     */
    public TrainTemplate(int engineId, @NotNull List<Integer> wagonTypes) {
        this.engineId = engineId;
        this.wagonTypes = Collections.unmodifiableList(new ArrayList<>(wagonTypes));
    }

    @Override
    public int hashCode() {
        return engineId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainTemplate)) return false;

        final TrainTemplate productionAtEngineShop = (TrainTemplate) obj;

        if (engineId != productionAtEngineShop.engineId) return false;
        return wagonTypes.equals(productionAtEngineShop.wagonTypes);
    }

    /**
     * @return
     */
    public int getEngineId() {
        return engineId;
    }

    /**
     * @return
     */
    public List<Integer> getWagonTypes() {
        return wagonTypes;
    }

    @Override
    public String toString() {
        return "engine type: " + engineId + ", with " + wagonTypes.size() + " wagons";
    }
}