/*
 * Copyright (C) Luke Lindsay
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

package jfreerails.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/**
*  This class renders a track piece.
*
*@author     Luke Lindsay
*     09 October 2001
*/
final public class TrackPieceRendererImpl implements TrackPieceRenderer {
    BufferedImage[] trackPieceIcons = new BufferedImage[512];
    private final String typeName;

    public void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g,
        int x, int y, java.awt.Dimension tileSize) {
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new java.lang.IllegalArgumentException("trackTemplate = " +
                trackTemplate + ", it should be in the range 0-511");
        }

        if (trackPieceIcons[trackTemplate] != null) {
            int drawX = x * tileSize.width - tileSize.width / 2;
            int drawY = y * tileSize.height - tileSize.height / 2;
            g.drawImage(trackPieceIcons[trackTemplate], drawX, drawY, null);
        }
    }

    public TrackPieceRendererImpl(ReadOnlyWorld w, ImageManager imageManager,
        int typeNumber) throws IOException {
        TrackRule trackRule = (TrackRule)w.get(KEY.TRACK_RULES, typeNumber);
        this.typeName = trackRule.getTypeName();

        for (int i = 0; i < 512; i++) {
            if (trackRule.testTrackPieceLegality(i)) {
                TrackConfiguration config = TrackConfiguration.getFlatInstance(i);
                String fileName = generateFilename(i);
                trackPieceIcons[i] = imageManager.getImage(fileName);
            }
        }
    }

    public BufferedImage getTrackPieceIcon(int trackTemplate) {
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new java.lang.IllegalArgumentException("trackTemplate = " +
                trackTemplate + ", it should be in the range 0-511");
        }

        return trackPieceIcons[trackTemplate];
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < 512; i++) {
            if (trackPieceIcons[i] != null) {
                String fileName = generateFilename(i);
                imageManager.setImage(fileName, trackPieceIcons[i]);
            }
        }
    }

    private String generateFilename(int i) {
        String relativeFileNameBase = "track" + File.separator +
            this.getTrackTypeName();
        int newTemplate = TrackConfiguration.getFlatInstance(i)
                                            .getNewTemplateNumber();
        String fileName = relativeFileNameBase + "_" +
            BinaryNumberFormatter.formatWithLowBitOnLeft(newTemplate, 8) +
            ".png";

        return fileName;
    }

    private String getTrackTypeName() {
        return typeName;
    }
}
