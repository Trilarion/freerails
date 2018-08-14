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

package freerails.model.cargo;

import freerails.model.ModelConstants;
import freerails.model.world.UnmodifiableWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CargoUtils {

    private CargoUtils() {
    }

    /**
     * The length of the returned array is the number of complete carloads of
     * the specified cargo category in the specified bundle. The values in the
     * array are the type of the cargo. E.g. if the bundle contained 2 carloads
     * of cargo type 3 and 1 of type 7, {3, 3, 7} would be returned.
     */
    public static Map<CargoCategory, List<Integer>> calculateCarLoads(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle) {
        // TODO overly complicated, easier way possible?
        int numCargoTypes = world.getCargos().size();
        Map<CargoCategory, Integer> numberOfCarLoads = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            numberOfCarLoads.put(cargoCategory, 0);
        }
        Map<CargoCategory, Map<Integer, Integer>> cars = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            Map<Integer, Integer> map = new HashMap<>();
            // TODO int i is not an ID, this will break if ids are not going from 0 to number of types - 1
            for (int i = 0; i < numCargoTypes; i++) {
                map.put(i, 0);
            }
            cars.put(cargoCategory, map);
        }
        for (int i = 0; i < numCargoTypes; i++) {
            Cargo ct = world.getCargo(i);
            int carsOfThisCargo = cargoBatchBundle.getAmountOfType(i) / ModelConstants.UNITS_OF_CARGO_PER_WAGON;
            numberOfCarLoads.put(ct.getCategory(), numberOfCarLoads.get(ct.getCategory()) + carsOfThisCargo);
            cars.get(ct.getCategory()).put(i, cars.get(ct.getCategory()).get(i) + carsOfThisCargo);
        }

        Map<CargoCategory, List<Integer>> returnMatrix = new HashMap<>();
        for (CargoCategory cargoCategory: CargoCategory.values()) {
            List<Integer> returnValue = new ArrayList<>();

            for (int cargoType = 0; cargoType < numCargoTypes; cargoType++) {
                for (int j = 0; j < cars.get(cargoCategory).get(cargoType); j++) {
                    returnValue.add(cargoType);
                }
            }
            returnMatrix.put(cargoCategory, returnValue);
        }
        return returnMatrix;
    }

    /**
     * Move the specified quantity of the specified cargo type from one bundle to another.
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
