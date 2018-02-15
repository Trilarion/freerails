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

import freerails.util.ui.ProgressMonitorModel;
import freerails.util.ui.ImageManager;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackRule;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;

/**
 * A list of TrackPieceRenderer.
 */
public class TrackPieceRendererList {

    private static final Logger logger = Logger.getLogger(TrackPieceRendererList.class.getName());
    private final TrackPieceRenderer[] trackPieceViewArray;

    /**
     * @param world
     * @param imageManager
     * @param progressMonitorModel
     * @throws IOException
     */
    public TrackPieceRendererList(ReadOnlyWorld world, ImageManager imageManager, ProgressMonitorModel progressMonitorModel) throws IOException {
        // Setup progress monitor..

        progressMonitorModel.nextStep(world.size(SharedKey.TrackRules));

        int progress = 0;
        progressMonitorModel.setValue(progress);

        int numberOfTrackTypes = world.size(SharedKey.TrackRules);
        trackPieceViewArray = new TrackPieceRenderer[numberOfTrackTypes];

        for (int i = 0; i < numberOfTrackTypes; i++) {
            trackPieceViewArray[i] = new TrackPieceRendererImpl(world, imageManager, i);
            progressMonitorModel.setValue(++progress);
        }
    }

    /**
     * @param i
     * @return
     */
    public TrackPieceRenderer getTrackPieceView(int i) {
        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER == i) {
            return NullTrackPieceRenderer.instance;
        }
        return trackPieceViewArray[i];
    }

    /**
     * @param world
     * @return
     */
    public boolean validate(ReadOnlyWorld world) {
        boolean okSoFar = true;

        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, i);
            Iterator<TrackConfiguration> legalConfigurationsIterator = trackRule.getLegalConfigurationsIterator();
            TrackPieceRenderer trackPieceView = getTrackPieceView(i);

            if (null == trackPieceView) {
                logger.warn("No track piece view for the following track type: " + trackRule.getTypeName());

                return false;
            }
            while (legalConfigurationsIterator.hasNext()) {
                TrackConfiguration trackConfig = legalConfigurationsIterator.next();
                int trackGraphicsNo = trackConfig.getConfiguration();
                Image img = trackPieceView.getTrackPieceIcon(trackGraphicsNo);

                if (null == img) {
                    logger.warn("No track piece image for the following track type: " + trackRule.getTypeName() + ", with configuration: " + trackGraphicsNo);
                    okSoFar = false;
                }
            }
        }

        return okSoFar;
    }
}