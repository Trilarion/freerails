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

/*
 * TrackPieceViewList.java
 *
 */
package freerails.client.renderer.track;

import freerails.model.track.TrackType;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.track.TrackConfiguration;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.*;
import java.io.IOException;

/**
 * A list of TrackPieceRenderer.
 */
public class TrackPieceRendererList {

    private static final TaggedLogger logger = Logger.tag("TrackPieceRendererList");
    private final UnmodifiableWorld world;
    private final TrackPieceRenderer[] trackPieceViewArray;

    /**
     * Creates a track piece renderer list instance.
     *
     * @param world the game world being queried
     * @param imageManager the image manager parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public TrackPieceRendererList(UnmodifiableWorld world, ImageManager imageManager) throws IOException {
        this.world = world;
        int numberOfTrackTypes = world.getTrackTypes().size();
        trackPieceViewArray = new TrackPieceRenderer[numberOfTrackTypes];

        for (int trackTypeIndex = 0; trackTypeIndex < numberOfTrackTypes; trackTypeIndex++) {
            int trackTypeId = world.getTrackTypeAtIndex(trackTypeIndex).getId();
            trackPieceViewArray[trackTypeIndex] = new TrackPieceRendererImpl(world, imageManager, trackTypeId);
        }
    }

    /**
     * Returns the track piece view.
     *
     * @param trackTypeId stable ID of the track type
     * @return the track piece view produced by this method
     */
    public TrackPieceRenderer getTrackPieceView(int trackTypeId) {
        return trackPieceViewArray[world.getTrackTypeIndex(trackTypeId)];
    }

    // TODO this should be a test, in the application we would just fail (no difference anyway)
    /**
     * Performs the validate operation.
     *
     * @param world the game world being queried
     * @return {@code true} if validate; otherwise, {@code false}
     */
    public boolean validate(UnmodifiableWorld world) {
        boolean okSoFar = true;

        for (TrackType trackType: world.getTrackTypes()) {
            TrackPieceRenderer trackPieceView = getTrackPieceView(trackType.getId());

            if (null == trackPieceView) {
                logger.warn("No track piece view for the following track type: " + trackType.getName());

                return false;
            }
            for (TrackConfiguration trackConfig: trackType.getValidTrackConfigurations()) {
                int trackGraphicsNo = trackConfig.getConfiguration();
                Image img = trackPieceView.getTrackPieceIcon(trackGraphicsNo);

                if (null == img) {
                    logger.warn("No track piece image for the following track type: " + trackType.getName() + ", with configuration: " + trackGraphicsNo);
                    okSoFar = false;
                }
            }
        }

        return okSoFar;
    }
}