/*
* ChequeredTileView.java
*
* Created on 07 July 2001, 14:25
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageSplitter;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class ChequeredTileRenderer extends AbstractTileRenderer {
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return (x + y) % 2;
    }

    /** Creates new ChequeredTileView */
    public ChequeredTileRenderer(ImageSplitter imageSplitter, int[] rgbValues,
        TerrainType tileModel) {
        super(tileModel, rgbValues);
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[2];

        for (int i = 0; i < tileIcons.length; i++) {
            tileIcons[i] = imageSplitter.getTileFromSubGrid(0 + i, 0);
        }
    }

    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.tileIcons = new Image[2];
        this.tileIcons[0] = imageManager.getImage(generateRelativeFileName(0));
        this.tileIcons[1] = imageManager.getImage(generateRelativeFileName(1));
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.tileIcons.length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.tileIcons[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}