package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/**
*  This class renders a track piece.
*
*@author     Luke Lindsay
*     09 October 2001
*/
final public class TrackPieceRendererImpl implements TrackPieceRenderer {
    Image[] trackPieceIcons = new Image[512];
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
        TrackRule trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, typeNumber);
        this.typeName = trackRule.getTypeName();

        for (int i = 0; i < 512; i++) {
            if (trackRule.testTrackPieceLegality(i)) {
                String fileName = generateFilename(i);
                trackPieceIcons[i] = imageManager.getImage(fileName);
            }
        }
    }

    public Image getTrackPieceIcon(int trackTemplate) {
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