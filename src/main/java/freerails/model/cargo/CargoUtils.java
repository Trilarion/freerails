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

import freerails.model.ModelConstants;
import freerails.model.world.UnmodifiableWorld;

import java.util.ArrayList;
import java.util.HashMap;
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
        // TODO overly complicated, easier way possible?
        Map<CargoCategory, Integer> numberOfCarLoads = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            numberOfCarLoads.put(cargoCategory, 0);
        }
        Map<CargoCategory, Map<Integer, Integer>> cars = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            Map<Integer, Integer> map = new HashMap<>();
            for (Cargo cargo : world.getCargos()) {
                map.put(cargo.getId(), 0);
            }
            cars.put(cargoCategory, map);
        }
        for (Cargo ct : world.getCargos()) {
            int cargoId = ct.getId();
            int carsOfThisCargo = cargoBatchBundle.getAmountOfType(cargoId) / ModelConstants.UNITS_OF_CARGO_PER_WAGON;
            numberOfCarLoads.put(ct.getCategory(), numberOfCarLoads.get(ct.getCategory()) + carsOfThisCargo);
            cars.get(ct.getCategory()).put(cargoId, cars.get(ct.getCategory()).get(cargoId) + carsOfThisCargo);
        }

        Map<CargoCategory, List<Integer>> returnMatrix = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            List<Integer> returnValue = new ArrayList<>();

            for (Cargo cargo : world.getCargos()) {
                int cargoId = cargo.getId();
                for (int j = 0; j < cars.get(cargoCategory).get(cargoId); j++) {
                    returnValue.add(cargoId);
                }
            }
            returnMatrix.put(cargoCategory, returnValue);
        }
        return returnMatrix;
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
        int amountTransferedSoFar = 0;

        for (CargoBatch cb: from.getCargoBatches()) {
            if (amountTransferedSoFar > amountToTransfer) {
                break;
            }

            if (cb.getCargoTypeId() == cargoTypeToTransfer) {
                int amount = from.getAmount(cb);
                int amountOfThisBatchToTransfer;

                if (amount < amountToTransfer - amountTransferedSoFar) {
                    amountOfThisBatchToTransfer = amount;
                    from.setAmount(cb, 0);
                } else {
                    amountOfThisBatchToTransfer = amountToTransfer - amountTransferedSoFar;
                    from.addCargo(cb, -amountOfThisBatchToTransfer);
                }

                to.addCargo(cb, amountOfThisBatchToTransfer);
                amountTransferedSoFar += amountOfThisBatchToTransfer;
            }
        }
    }
}
