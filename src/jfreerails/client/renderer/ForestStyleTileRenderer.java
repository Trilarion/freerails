/*
* ForestStyleTileView.java
*
* Created on 07 July 2001, 14:36
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageSplitter;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class ForestStyleTileRenderer
    extends jfreerails.client.renderer.AbstractTileRenderer {
    private static final int[] X_LOOK_AT = {-1, 1};
    private static final int[] Y_LOOK_AT = {0, 0};

    /** Creates new ForestStyleTileView */
    public ForestStyleTileRenderer(ImageSplitter imageSplitter,
        int[] rgbValues, TerrainType tileModel) {
        super(tileModel, rgbValues);
        imageSplitter.setTransparencyToOPAQUE();
        setTileIcons(new java.awt.Image[4]);

        //Grap them in this order so that they display correctly :)
        getTileIcons()[0] = imageSplitter.getTileFromSubGrid(0, 0);
        getTileIcons()[1] = imageSplitter.getTileFromSubGrid(1, 0);
        getTileIcons()[2] = imageSplitter.getTileFromSubGrid(3, 0);
        getTileIcons()[3] = imageSplitter.getTileFromSubGrid(2, 0);
    }

    public ForestStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.setTileIcons(new Image[4]);

        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            this.getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 2; i++) {
            iconNumber = iconNumber |
                checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
            iconNumber = iconNumber << 1;
        }

        iconNumber = iconNumber >> 1;

        return iconNumber;
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.format(i, 2);
    }
}