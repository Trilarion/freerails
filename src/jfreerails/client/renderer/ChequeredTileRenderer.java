/*
* ChequeredTileView.java
*
* Created on 07 July 2001, 14:25
*/
package jfreerails.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import jfreerails.client.common.ImageManager;
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

    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.setTileIcons(new BufferedImage[2]);
        this.getTileIcons()[0] = imageManager.getImage(generateRelativeFileName(
                    0));
        this.getTileIcons()[1] = imageManager.getImage(generateRelativeFileName(
                    1));
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}
