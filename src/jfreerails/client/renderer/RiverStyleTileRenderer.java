/*
* RiverStyleTileIconSelecter.java
*
* Created on 07 July 2001, 12:36
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class RiverStyleTileRenderer
    extends jfreerails.client.renderer.AbstractTileRenderer {
    private static final int[] Y_LOOK_AT = {0, 1, 0, -1};
    private static final int[] X_LOOK_AT = {-1, 0, 1, 0};

    /** Creates new RiverStyleTileView */
    public RiverStyleTileRenderer(
        jfreerails.client.common.ImageSplitter imageSplitter, int[] rgbValues,
        TerrainType tileModel) {
        super(tileModel, rgbValues);
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[16];

        for (int i = 0; i < tileIcons.length; i++) {
            tileIcons[i] = imageSplitter.getTileFromSubGrid(15 - i, 0);
        }
    }

    public RiverStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.tileIcons = new Image[16];

        for (int i = 0; i < this.tileIcons.length; i++) {
            String fileName = generateRelativeFileName(i);
            this.tileIcons[i] = imageManager.getImage(fileName);
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 4; i++) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber |
                checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
        }

        return iconNumber;
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.tileIcons.length; i++) {
            imageManager.setImage(generateRelativeFileName(i), this.tileIcons[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.formatWithLowBitOnLeft(i, 4);
    }
}