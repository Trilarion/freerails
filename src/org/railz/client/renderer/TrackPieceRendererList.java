/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * TrackPieceViewList.java
 *
 * Created on 21 July 2001, 01:04
 */
package org.railz.client.renderer;

import java.awt.Image;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.*;

import org.railz.client.common.ImageManager;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.TrackRule;


final public class TrackPieceRendererList {
    private static final Logger logger = Logger.getLogger("global");

    private final TrackPieceRenderer[] trackPieceViewArray;

    public TrackPieceRenderer getTrackPieceView(int i) {
	return trackPieceViewArray[i];
    }

    public TrackPieceRendererList(ReadOnlyWorld w, ImageManager imageManager,
        FreerailsProgressMonitor pm) throws IOException {
        //		Setup progress monitor..
        pm.setMessage("Loading track graphics.");
        pm.setMax(w.size(KEY.TRACK_RULES));

        int progress = 0;
        pm.setValue(progress);

        int numberOfTrackTypes = w.size(KEY.TRACK_RULES);
        trackPieceViewArray = new TrackPieceRenderer[numberOfTrackTypes];

        for (int i = 0; i < numberOfTrackTypes; i++) {
            trackPieceViewArray[i] = new TrackPieceRendererImpl(w,
                    imageManager, i);
            pm.setValue(++progress);
        }
    }

    public boolean validate(ReadOnlyWorld w) {
        boolean okSoFar = true;

        for (int i = 0; i < w.size(KEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule)w.get(KEY.TRACK_RULES, i);
            TrackPieceRenderer trackPieceView = this.getTrackPieceView(i);

            if (null == trackPieceView) {
                logger.log(Level.WARNING,
                    "No track piece view for the following track type: " +
                    trackRule.toString());

                return false;
            } else {
		for (byte j = Byte.MIN_VALUE; j < Byte.MAX_VALUE; j++) {
			if (!trackRule.testTrackPieceLegality(j))
			    continue;
                    Image img = trackPieceView.getTrackPieceIcon(j);
                    if (null == img) {
                        logger.log(Level.WARNING, 
                            "No track piece image for the following track type: " +
                            trackRule.toString() + ", with configuration: " +
                            j);
                        okSoFar = false;
                    }
                }
            }
        }

        return okSoFar;
    }
}
