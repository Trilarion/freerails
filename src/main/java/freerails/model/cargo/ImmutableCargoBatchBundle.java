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
package freerails.model.cargo;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An immutable point-in-time copy of a cargo bundle.
 */
public final class ImmutableCargoBatchBundle implements UnmodifiableCargoBatchBundle {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final ImmutableCargoBatchBundle EMPTY = new ImmutableCargoBatchBundle(CargoBatchBundle.EMPTY);

    private final SortedMap<CargoBatch, Integer> cargoMap;

    /**
     * Copies the supplied bundle so subsequent changes to it cannot affect this snapshot.
     *
     * @param bundle bundle to copy
     */
    public ImmutableCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle bundle) {
        Objects.requireNonNull(bundle);
        SortedMap<CargoBatch, Integer> copy = new TreeMap<>();
        for (CargoBatch cargoBatch : bundle.getCargoBatches()) {
            int amount = bundle.getAmount(cargoBatch);
            if (amount != 0) {
                copy.put(cargoBatch, amount);
            }
        }
        cargoMap = Collections.unmodifiableSortedMap(copy);
    }

    @Override
    public Collection<CargoBatch> getCargoBatches() {
        return Collections.unmodifiableList(new ArrayList<>(cargoMap.keySet()));
    }

    @Override
    public boolean contains(CargoBatch cargoBatch) {
        return cargoMap.containsKey(cargoBatch);
    }

    @Override
    public int getAmount(CargoBatch cargoBatch) {
        return cargoMap.getOrDefault(cargoBatch, 0);
    }

    @Override
    public int getAmountOfType(int cargoType) {
        int amount = 0;
        for (var entry : cargoMap.entrySet()) {
            if (entry.getKey().getCargoTypeId() == cargoType) {
                amount += entry.getValue();
            }
        }
        return amount;
    }

    @Override
    public int size() {
        return cargoMap.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UnmodifiableCargoBatchBundle bundle) || size() != bundle.size()) {
            return false;
        }
        for (var entry : cargoMap.entrySet()) {
            if (bundle.getAmount(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return cargoMap.size();
    }

    @Override
    public String toString() {
        return new CargoBatchBundle(this).toString();
    }
}
