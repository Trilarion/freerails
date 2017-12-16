/*
 * ForestStyleTileView.java
 *
 * Created on 07 July 2001, 14:36
 */
package freerails.client.renderer;

import freerails.client.common.BinaryNumberFormatter;
import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.IOException;

/**
 * Looks to see whether the tiles to the left and right of the same type when
 * deciding which tile icon to use.
 *
 * @author Luke Lindsay
 */
final public class ForestStyleTileRenderer extends
        freerails.client.renderer.AbstractTileRenderer {
    private static final int[] X_LOOK_AT = {-1, 1};

    private static final int[] Y_LOOK_AT = {0, 0};

    public ForestStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
                                   TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[4]);

        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            this.getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 2; i++) {
            iconNumber = iconNumber
                    | checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
            iconNumber = iconNumber << 1;
        }

        iconNumber = iconNumber >> 1;

        return iconNumber;
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
        return BinaryNumberFormatter.format(i, 2);
    }
}