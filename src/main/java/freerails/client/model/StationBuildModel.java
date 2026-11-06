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

package freerails.client.model;

import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.controller.StationBuilder;
import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.Status;
import freerails.util.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Coordinates station selection, placement, and cancellation independently of the UI toolkit.
 */
public class StationBuildModel {

    private final StationBuilder stationBuilder;
    private final ModelRoot modelRoot;
    private final Set<Integer> stationTypeIds = new HashSet<>();
    private boolean stationTypeSelected;

    /**
     * Creates a station build model for the current world.
     *
     * @param stationBuilder builds stations in the current world
     * @param modelRoot provides the current game model
     */
    public StationBuildModel(StationBuilder stationBuilder, ModelRoot modelRoot) {
        this.stationBuilder = stationBuilder;
        this.modelRoot = modelRoot;
        UnmodifiableWorld world = modelRoot.getWorld();
        for (TrackType trackType : world.getTrackTypes()) {
            if (trackType.isStation()) {
                stationTypeIds.add(trackType.getId());
            }
        }
    }

    /**
     * Selects a station type for placement.
     *
     * @param ruleId station track-type identifier
     * @throws IllegalArgumentException if the world has no such station type
     */
    public void chooseStationType(int ruleId) {
        if (!stationTypeIds.contains(ruleId)) {
            throw new IllegalArgumentException("Unknown station track type: " + ruleId);
        }
        stationBuilder.setStationType(ruleId);
        stationTypeSelected = true;
    }

    /**
     * Builds a station at the supplied map location using the selected type.
     *
     * @param location target map tile
     * @return the station build status
     */
    public Status buildStationAt(Vector2D location) {
        if (!stationTypeSelected) {
            return Status.fail("Select a station type before building a station.");
        }
        Status status = stationBuilder.buildStation(location);
        modelRoot.setProperty(
                ModelRootProperty.CURSOR_MESSAGE,
                status.isSuccess() ? null : status.getMessage());
        if (status.isSuccess()) {
            stationTypeSelected = false;
        }
        return status;
    }

    /**
     * Cancels station placement and clears its active build state.
     */
    public void cancelStationPlacement() {
        stationTypeSelected = false;
    }

    /**
     * Returns whether a station type is currently selected for placement.
     *
     * @return whether the model has an active station type
     */
    public boolean isStationTypeSelected() {
        return stationTypeSelected;
    }
}
