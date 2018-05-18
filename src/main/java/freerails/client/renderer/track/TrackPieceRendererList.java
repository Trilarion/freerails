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

/*
 * TrackPieceViewList.java
 *
 */
package freerails.client.renderer.track;

import freerails.model.track.TrackType;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.track.TrackConfiguration;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;

/**
 * A list of TrackPieceRenderer.
 */
public class TrackPieceRendererList {

    private static final Logger logger = Logger.getLogger(TrackPieceRendererList.class.getName());
    private final TrackPieceRenderer[] trackPieceViewArray;

    /**
     * @param world
     * @param imageManager
     * @throws IOException
     */
    public TrackPieceRendererList(UnmodifiableWorld world, ImageManager imageManager) throws IOException {
        int numberOfTrackTypes = world.getTrackTypes().size();
        trackPieceViewArray = new TrackPieceRenderer[numberOfTrackTypes];

        for (int i = 0; i < numberOfTrackTypes; i++) {
            trackPieceViewArray[i] = new TrackPieceRendererImpl(world, imageManager, i);
        }
    }

    /**
     * @param i
     * @return
     */
    public TrackPieceRenderer getTrackPieceView(int i) {
        return trackPieceViewArray[i];
    }

    // TODO this should be a test, in the application we would just fail (no difference anyway)
    /**
     * @param world
     * @return
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