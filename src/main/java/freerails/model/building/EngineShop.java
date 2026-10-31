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

package freerails.model.building;

import freerails.model.operations.train.TrainTemplate;
import freerails.util.Identifiable;
import freerails.util.Vector2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A facility at a station where the player queues new trains for construction.
 */
public class EngineShop extends Identifiable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Vector2D location;
    private final List<TrainTemplate> production = new ArrayList<>();

    /**
     * Creates an empty engine shop at a map location.
     *
     * @param id stable engine-shop identifier
     * @param location station location of the shop
     */
    public EngineShop(int id, @NotNull Vector2D location) {
        super(id);
        this.location = new Vector2D(location);
    }

    /**
     * Creates a copy including the queued train templates.
     *
     * @param other shop to copy
     */
    public EngineShop(@NotNull EngineShop other) {
        super(other.getId());
        location = new Vector2D(other.location);
        production.addAll(other.production);
    }

    /**
     * Returns the shop's map location.
     *
     * @return the shop location
     */
    public Vector2D getLocation() {
        return location;
    }

    /**
     * Returns an unmodifiable view of the queued train templates.
     *
     * @return the ordered production queue
     */
    public List<TrainTemplate> getProduction() {
        return Collections.unmodifiableList(production);
    }

    /**
     * Replaces the production queue with a defensive copy.
     *
     * @param production ordered train templates to queue
     */
    public void setProduction(@NotNull List<TrainTemplate> production) {
        List<TrainTemplate> productionCopy = List.copyOf(production);
        this.production.clear();
        this.production.addAll(productionCopy);
    }
}
