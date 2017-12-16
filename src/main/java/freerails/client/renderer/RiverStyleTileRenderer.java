/*
 * RiverStyleTileIconSelecter.java
 *
 * Created on 07 July 2001, 12:36
 */
package freerails.client.renderer;

import java.awt.Image;
import java.io.IOException;

import freerails.client.common.BinaryNumberFormatter;
import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

/**
 * Selects a tile icon to use based on the type of the tiles to the North, East,
 * South and West.
 * 
 * @author Luke Lindsay
 */
final public class RiverStyleTileRenderer extends
        freerails.client.renderer.AbstractTileRenderer {
    private static final int[] Y_LOOK_AT = { 0, 1, 0, -1 };

    private static final int[] X_LOOK_AT = { -1, 0, 1, 0 };

    public RiverStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
            TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[16]);

        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            this.getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /** 666 optimize cache */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 4; i++) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber
                    | checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
        }

        return iconNumber;
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            imageManager.setImage(generateRelativeFileName(i), this
                    .getTileIcons()[i]);
        }
    }

    @Override
    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.formatWithLowBitOnLeft(i, 4);
    }
}