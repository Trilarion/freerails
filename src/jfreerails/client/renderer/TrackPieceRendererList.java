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
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import java.util.Iterator;
import jfreerails.client.common.ImageManager;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


final public class TrackPieceRendererList {
    private final TrackPieceRenderer[] trackPieceViewArray;

    public TrackPieceRenderer getTrackPieceView(int i) {
        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER == i) {
            return NullTrackPieceRenderer.instance;
        } else {
            return trackPieceViewArray[i];
        }
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
            Iterator legalConfigurationsIterator = trackRule.getLegalConfigurationsIterator();
            TrackPieceRenderer trackPieceView = this.getTrackPieceView(i);

            if (null == trackPieceView) {
                System.err.println(
                    "No track piece view for the following track type: " +
                    trackRule.getTypeName());

                return false;
            } else {
                while (legalConfigurationsIterator.hasNext()) {
                    TrackConfiguration trackConfig = (TrackConfiguration)legalConfigurationsIterator.next();
                    int trackGraphicsNo = trackConfig.getTrackGraphicsNumber();
                    Image img = trackPieceView.getTrackPieceIcon(trackGraphicsNo);

                    if (null == img) {
                        System.err.println(
                            "No track piece image for the following track type: " +
                            trackRule.getTypeName() + ", with configuration: " +
                            trackGraphicsNo);
                        okSoFar = false;
                    }
                }
            }
        }

        return okSoFar;
    }
}