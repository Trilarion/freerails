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

package freerails.client.renderer;

import freerails.client.common.BinaryNumberFormatter;
import freerails.client.common.ImageManager;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * This class renders a track piece.
 */
public final class TrackPieceRendererImpl implements TrackPieceRenderer {
    private final Image[] trackPieceIcons = new Image[512];

    private final String typeName;

    /**
     * @param w
     * @param imageManager
     * @param typeNumber
     * @throws IOException
     */
    public TrackPieceRendererImpl(ReadOnlyWorld w, ImageManager imageManager,
                                  int typeNumber) throws IOException {
        TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, typeNumber);
        this.typeName = trackRule.getTypeName();

        for (int i = 0; i < 512; i++) {
            if (trackRule.testTrackPieceLegality(i)) {
                String fileName = generateFilename(i, typeName);
                trackPieceIcons[i] = imageManager.getImage(fileName);
            }
        }
    }

    /**
     * @param i
     * @param trackTypeName
     * @return
     */
    public static String generateFilename(int i, String trackTypeName) {
        String relativeFileNameBase = "track" + File.separator + trackTypeName;
        int newTemplate = TrackConfiguration.from9bitTemplate(i)
                .get8bitTemplate();

        return relativeFileNameBase + '_'
                + BinaryNumberFormatter.formatWithLowBitOnLeft(newTemplate, 8)
                + ".png";
    }

    /**
     * @param trackTemplate
     * @param g
     * @param x
     * @param y
     * @param tileSize
     */
    public void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g,
                                   int x, int y, java.awt.Dimension tileSize) {
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new java.lang.IllegalArgumentException("trackTemplate = "
                    + trackTemplate + ", it should be in the range 0-511");
        }

        if (trackPieceIcons[trackTemplate] != null) {
            int drawX = x * tileSize.width - tileSize.width / 2;
            int drawY = y * tileSize.height - tileSize.height / 2;
            g.drawImage(trackPieceIcons[trackTemplate], drawX, drawY, null);
        }
    }

    /**
     * @param trackTemplate
     * @return
     */
    public Image getTrackPieceIcon(int trackTemplate) {
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new java.lang.IllegalArgumentException("trackTemplate = "
                    + trackTemplate + ", it should be in the range 0-511");
        }

        return trackPieceIcons[trackTemplate];
    }

    private String getTrackTypeName() {
        return typeName;
    }
}