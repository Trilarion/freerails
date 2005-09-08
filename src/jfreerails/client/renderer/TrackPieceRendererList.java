/*
 * TrackPieceViewList.java
 *
 * Created on 21 July 2001, 01:04
 */
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import jfreerails.client.common.ImageManager;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;

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
						.warning("No track piece view for the following track type: "
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
							.warning("No track piece image for the following track type: "
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