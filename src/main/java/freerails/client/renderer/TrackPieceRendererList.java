/*
 * TrackPieceViewList.java
 *
 * Created on 21 July 2001, 01:04
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.util.FreerailsProgressMonitor;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;

/**
 * A list of TrackPieceRenderers.
 *
 * @author Luke
 */
final public class TrackPieceRendererList {
    private static final Logger logger = Logger
            .getLogger(TrackPieceRendererList.class.getName());

    private final TrackPieceRenderer[] trackPieceViewArray;

    public TrackPieceRenderer getTrackPieceView(int i) {
        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER == i) {
            return NullTrackPieceRenderer.instance;
        }
        return trackPieceViewArray[i];
    }

    public TrackPieceRendererList(ReadOnlyWorld w, ImageManager imageManager,
                                  FreerailsProgressMonitor pm) throws IOException {
        // Setup progress monitor..

        pm.nextStep(w.size(SKEY.TRACK_RULES));

        int progress = 0;
        pm.setValue(progress);

        int numberOfTrackTypes = w.size(SKEY.TRACK_RULES);
        trackPieceViewArray = new TrackPieceRenderer[numberOfTrackTypes];

        for (int i = 0; i < numberOfTrackTypes; i++) {
            trackPieceViewArray[i] = new TrackPieceRendererImpl(w,
                    imageManager, i);
            pm.setValue(++progress);
        }
    }

    public boolean validate(ReadOnlyWorld w) {
        boolean okSoFar = true;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
            Iterator<TrackConfiguration> legalConfigurationsIterator = trackRule
                    .getLegalConfigurationsIterator();
            TrackPieceRenderer trackPieceView = this.getTrackPieceView(i);

            if (null == trackPieceView) {
                logger
                        .warn("No track piece view for the following track type: "
                                + trackRule.getTypeName());

                return false;
            }
            while (legalConfigurationsIterator.hasNext()) {
                TrackConfiguration trackConfig = legalConfigurationsIterator
                        .next();
                int trackGraphicsNo = trackConfig.getTrackGraphicsID();
                Image img = trackPieceView.getTrackPieceIcon(trackGraphicsNo);

                if (null == img) {
                    logger
                            .warn("No track piece image for the following track type: "
                                    + trackRule.getTypeName()
                                    + ", with configuration: "
                                    + trackGraphicsNo);
                    okSoFar = false;
                }
            }
        }

        return okSoFar;
    }
}