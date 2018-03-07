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

package freerails.model.train;

import freerails.model.WorldConstants;
import freerails.util.ImmutableList;

import java.io.Serializable;

/**
 * Represents a train.
 */
public class Train implements Serializable {

    private static final long serialVersionUID = 3545235825756812339L;
    private final int scheduleId;
    // TODO replace engine type id with an enum maybe? or by a class?
    private final int engineTypeId;
    private final ImmutableList<Integer> wagonTypes;
    private final int cargoBundleId;

    /**
     * @param engine
     * @param wagons
     * @param scheduleID
     * @param BundleId
     */
    public Train(int engine, ImmutableList<Integer> wagons, int scheduleID, int BundleId) {
        engineTypeId = engine;
        wagonTypes = wagons;
        scheduleId = scheduleID;
        cargoBundleId = BundleId;
    }

    /**
     * @param wagons
     * @param BundleId
     */
    public Train(ImmutableList<Integer> wagons, int BundleId) {
        wagonTypes = wagons;
        cargoBundleId = BundleId;
        engineTypeId = 0;
        scheduleId = 0;
    }

    /**
     * @param engine
     * @param wagons
     * @param scheduleID
     */
    public Train(int engine, ImmutableList<Integer> wagons, int scheduleID) {
        engineTypeId = engine;
        wagonTypes = wagons;
        scheduleId = scheduleID;
        cargoBundleId = 0;
    }

    @Override
    public int hashCode() {
        int result;
        result = scheduleId;
        result = 29 * result + engineTypeId;
        result = 29 * result + cargoBundleId;

        return result;
    }

    /**
     * @param newEngine
     * @param newWagons
     * @return
     */
    public Train getNewInstance(int newEngine, ImmutableList<Integer> newWagons) {
        return new Train(newEngine, newWagons, scheduleId, cargoBundleId);
    }

    /**
     * @return
     */
    public int getLength() {
        return (1 + wagonTypes.size()) * WorldConstants.WAGON_LENGTH; // Engine + wagons.
    }

    /**
     * @return
     */
    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    /**
     * @param i
     * @return
     */
    public int getWagon(int i) {
        return wagonTypes.get(i);
    }

    /**
     * @return
     */
    public int getEngineType() {
        return engineTypeId;
    }

    /**
     * @return
     */
    public int getCargoBundleID() {
        return cargoBundleId;
    }

    /**
     * @return
     */
    public int getScheduleID() {
        return scheduleId;
    }

    /**
     * @return
     */
    public ImmutableList<Integer> getConsist() {
        return wagonTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Train) {
            Train test = (Train) obj;

            return cargoBundleId == test.cargoBundleId && engineTypeId == test.engineTypeId && wagonTypes.equals(test.wagonTypes) && scheduleId == test.scheduleId;
        }
        return false;
    }
}