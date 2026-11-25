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

package freerails.client.renderer.track;

import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.ui.ImageManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Track piece renderers keyed by their stable track type IDs.
 */
public class TrackPieceRendererList {

    private final Map<Integer, TrackPieceRenderer> trackPieceRenderers;

    /**
     * Loads one track piece renderer for every track type of the world.
     *
     * @param world the game world being queried
     * @param imageManager the image manager parameter
     * @throws IOException if the image of a valid track configuration cannot be loaded
     */
    public TrackPieceRendererList(UnmodifiableWorld world, ImageManager imageManager) throws IOException {
        Map<Integer, TrackPieceRenderer> renderers = new HashMap<>();
        for (TrackType trackType : world.getTrackTypes()) {
            renderers.put(trackType.getId(), new TrackPieceRendererImpl(world, imageManager, trackType.getId()));
        }
        trackPieceRenderers = Map.copyOf(renderers);
    }

    /**
     * Returns the track piece renderer for a track type.
     *
     * @param trackTypeId stable ID of the track type
     * @return the track piece renderer for the track type
     * @throws IllegalArgumentException if no renderer exists for the track type ID
     */
    public TrackPieceRenderer getTrackPieceView(int trackTypeId) {
        TrackPieceRenderer renderer = trackPieceRenderers.get(trackTypeId);
        if (renderer == null) {
            throw new IllegalArgumentException("Unknown track type ID: " + trackTypeId);
        }
        return renderer;
    }

    /**
     * Checks whether this list has exactly one renderer for each track type of
     * the specified world. The client uses this to decide whether already
     * loaded renderers can be reused for a newly received world.
     * <p>
     * Only track type IDs are compared. Every renderer already holds an image
     * for each valid configuration of its track type, because loading fails
     * otherwise.
     *
     * @param world the world
     * @return whether these renderers cover the world's track types
     */
    public boolean validate(UnmodifiableWorld world) {
        Set<Integer> trackTypeIds = world.getTrackTypes().stream()
                .map(TrackType::getId)
                .collect(Collectors.toSet());
        return trackTypeIds.equals(trackPieceRenderers.keySet());
    }
}
