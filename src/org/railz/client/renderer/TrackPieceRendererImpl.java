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

package org.railz.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.railz.client.common.BinaryNumberFormatter;
import org.railz.client.common.ImageManager;
import org.railz.client.config.ClientConfigurationConstants;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.TrackRule;

/**
 * This class renders a track piece.
 * 
 * @author Luke Lindsay 09 October 2001
 */
final public class TrackPieceRendererImpl implements TrackPieceRenderer {

	BufferedImage[] trackPieceIcons = new BufferedImage[256];
	private final String typeName;

	public TrackPieceRendererImpl(ReadOnlyWorld w, ImageManager imageManager,
			int typeNumber) throws IOException {

		TrackRule trackRule = (TrackRule) w.get(KEY.TRACK_RULES, typeNumber);
		this.typeName = trackRule.toString();

		for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
			if (trackRule.testTrackPieceLegality(i)) {
				String fileName = generateFilename(i);
				trackPieceIcons[i & 0xFF] = imageManager.getImage(fileName);
			}
		}
	}

	@Override
	public void drawTrackPieceIcon(byte trackConfig, java.awt.Graphics g,
			int x, int y, java.awt.Dimension tileSize) {

		int trackTemplate = trackConfig & 0xFF;
		if (trackPieceIcons[trackTemplate] != null) {
			int drawX = x * tileSize.width - tileSize.width / 2;
			int drawY = y * tileSize.height - tileSize.height / 2;
			g.drawImage(trackPieceIcons[trackTemplate], drawX, drawY, null);
		}
	}

	@Override
	public BufferedImage getTrackPieceIcon(byte trackConfig) {
		int trackTemplate = trackConfig & 0xFF;
		return trackPieceIcons[trackTemplate];
	}

	@Override
	public void dumpImages(ImageManager imageManager) {
		for (int i = 0; i < 256; i++) {
			if (trackPieceIcons[i] != null) {
				String fileName = generateFilename((byte) i);
				imageManager.setImage(fileName, trackPieceIcons[i]);
			}
		}
	}

	private String generateFilename(byte trackTemplate) {
		String relativeFileNameBase = "track" + "/" + this.getTrackTypeName();
		int newTemplate = trackTemplate & 0xFF;
		String fileName = relativeFileNameBase + "_"
				+ BinaryNumberFormatter.format(newTemplate, 8)
				+ ClientConfigurationConstants.IMAGE_FILE_EXTENSION;

		return fileName;
	}

	private String getTrackTypeName() {
		return typeName;
	}
}
