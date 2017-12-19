/*
 * ChequeredTileView.java
 *
 * Created on 07 July 2001, 14:25
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.IOException;

/**
 * Paints 2 variations of a tile icon a chequered pattern.
 *
 */
final public class ChequeredTileRenderer extends AbstractTileRenderer {

    /**
     *
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param w
     * @throws IOException
     */
    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues,
                                 TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[2]);
        this.getTileIcons()[0] = imageManager
                .getImage(generateRelativeFileName(0));
        this.getTileIcons()[1] = imageManager
                .getImage(generateRelativeFileName(1));
    }

    /**
     *
     * @param x
     * @param y
     * @param w
     * @return
     */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return (x + y) % 2;
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}