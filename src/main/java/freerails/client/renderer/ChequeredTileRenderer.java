/*
 * ChequeredTileView.java
 *
 * Created on 07 July 2001, 14:25
 */
package freerails.client.renderer;

import java.awt.Image;
import java.io.IOException;

import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

/**
 * Paints 2 variations of a tile icon a chequered pattern.
 * 
 * @author Luke Lindsay
 */
final public class ChequeredTileRenderer extends AbstractTileRenderer {
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return (x + y) % 2;
    }

    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues,
            TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[2]);
        this.getTileIcons()[0] = imageManager
                .getImage(generateRelativeFileName(0));
        this.getTileIcons()[1] = imageManager
                .getImage(generateRelativeFileName(1));
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    @Override
    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}