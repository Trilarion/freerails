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

package freerails.client;

import freerails.client.model.StationBuildModel;
import freerails.client.renderer.RendererRoot;
import freerails.controller.StationBuilder;
import freerails.controller.TrackMoveProducer;
import freerails.model.world.UnmodifiableWorld;

/**
 * Provides toolkit-independent access to game actions and build workflows.
 */
public class ActionRoot {

    private StationBuildModel stationBuildModel;
    private TrackMoveProducer trackMoveProducer;

    /**
     * Creates an action root for the active game client.
     */
    public ActionRoot() {}

    /**
     * Returns the station build model.
     *
     * @return the station build model produced by this method
     */
    public StationBuildModel getStationBuildModel() {
        return stationBuildModel;
    }

    /**
     * Returns the track move producer.
     *
     * @return the track move producer produced by this method
     */
    public TrackMoveProducer getTrackMoveProducer() {
        return trackMoveProducer;
    }

    /**
     * Call this method when a new game is started or a game is loaded.
     */
    public void setup(ModelRootImpl modelRoot, RendererRoot rendererRoot) {
        UnmodifiableWorld world = modelRoot.getWorld();

        if (!world.getTrackTypes().isEmpty()) {
            trackMoveProducer = new TrackMoveProducer(modelRoot);
            stationBuildModel = new StationBuildModel(new StationBuilder(modelRoot), modelRoot);
        }
    }

}