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

package freerails.model.operations.cargo;

import freerails.model.ModelConstants;
import freerails.model.world.UnmodifiableWorld;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Utility methods for calculating and transferring cargo quantities. */
public class CargoUtils {

    /** Prevents instantiation of this utility class. */
    private CargoUtils() {
    }

    /**
     * Groups the complete wagon-loads in a bundle by cargo category. Each
     * returned list contains one cargo type identifier per complete carload;
     * partial loads are omitted.
     *
     * @param world world providing cargo definitions
     * @param cargoBatchBundle bundle whose loads are counted
     * @return lists of cargo identifiers, grouped by category
     */
    public static Map<CargoCategory, List<Integer>> calculateCarLoads(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle) {
        Map<CargoCategory, List<Integer>> carLoadsByCategory = new EnumMap<>(CargoCategory.class);
        for (CargoCategory category : CargoCategory.values()) {
            carLoadsByCategory.put(category, new ArrayList<>());
        }

        for (Cargo cargo : world.getCargos()) {
            int cargoId = cargo.getId();
            int carLoads = cargoBatchBundle.getAmountOfType(cargoId)
                    / ModelConstants.UNITS_OF_CARGO_PER_WAGON;
            List<Integer> categoryCarLoads = carLoadsByCategory.get(cargo.getCategory());
            for (int i = 0; i < carLoads; i++) {
                categoryCarLoads.add(cargoId);
            }
        }
        return carLoadsByCategory;
    }

    /**
     * Moves up to the requested amount of a cargo type between mutable bundles,
     * preserving the source batches.
     *
     * @param cargoTypeToTransfer cargo type identifier to move
     * @param amountToTransfer maximum quantity to move
     * @param from source bundle to remove cargo from
     * @param to destination bundle to receive cargo
     */
    public static void transferCargo(int cargoTypeToTransfer, int amountToTransfer, CargoBatchBundle from, CargoBatchBundle to) {
        if (0 == amountToTransfer) {
            return;
        }
        int amountTransferredSoFar = 0;

        for (CargoBatch cb: from.getCargoBatches()) {
            if (amountTransferredSoFar > amountToTransfer) {
                break;
            }

            if (cb.getCargoTypeId() == cargoTypeToTransfer) {
                int amount = from.getAmount(cb);
                int amountOfThisBatchToTransfer;

                if (amount < amountToTransfer - amountTransferredSoFar) {
                    amountOfThisBatchToTransfer = amount;
                    from.setAmount(cb, 0);
                } else {
                    amountOfThisBatchToTransfer = amountToTransfer - amountTransferredSoFar;
                    from.addCargo(cb, -amountOfThisBatchToTransfer);
                }

                to.addCargo(cb, amountOfThisBatchToTransfer);
                amountTransferredSoFar += amountOfThisBatchToTransfer;
            }
        }
    }
}
