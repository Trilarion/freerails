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

package freerails.client.renderer.track;

import freerails.model.track.TrackType;
import freerails.util.BinaryNumberFormatter;
import freerails.util.Vec2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.track.TrackConfiguration;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Renders a track piece.
 */
public class TrackPieceRendererImpl implements TrackPieceRenderer {

    private final Image[] trackPieceIcons = new Image[512];

    /**
     * @param world
     * @param imageManager
     * @param typeNumber
     * @throws IOException
     */
    public TrackPieceRendererImpl(UnmodifiableWorld world, ImageManager imageManager, int typeNumber) throws IOException {
        TrackType trackType = world.getTrackType(typeNumber);
        String typeName = trackType.getName();

        for (int i = 0; i < 512; i++) {
            if (trackType.testTrackPieceLegality(i)) {
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
        int newTemplate = TrackConfiguration.from9bitTemplate(i).get8bitTemplate();

        return relativeFileNameBase + '_' + BinaryNumberFormatter.formatWithLowBitOnLeft(newTemplate, 8) + ".png";
    }

    /**
     * @param g
     * @param trackTemplate
     * @param tileLocation
     * @param tileSize
     */
    @Override
    public void drawTrackPieceIcon(Graphics g, int trackTemplate, Vec2D tileLocation, Vec2D tileSize) {
        if (trackTemplate > 511 || trackTemplate < 0) {
            throw new java.lang.IllegalArgumentException("trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
        }

        if (trackPieceIcons[trackTemplate] != null) {
            Vec2D drawLocation = Vec2D.subtract(Vec2D.multiply(tileLocation, tileSize), Vec2D.divide(tileSize, 2));
            g.drawImage(trackPieceIcons[trackTemplate], drawLocation.x, drawLocation.y, null);
        }
    }

    /**
     * @param trackTemplate
     * @return
     */
    @Override
    public Image getTrackPieceIcon(int trackTemplate) {
        if (trackTemplate > 511 || trackTemplate < 0) {
            throw new java.lang.IllegalArgumentException("trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
        }

        return trackPieceIcons[trackTemplate];
    }
}