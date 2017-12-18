/*
 * StandardTileIconSelecter.java
 *
 * Created on 07 July 2001, 12:11
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Paints a tile for which there only one tile icon.
 *
 * @author Luke Lindsay
 */
final public class StandardTileRenderer extends
        freerails.client.renderer.AbstractTileRenderer {
    public StandardTileRenderer(ImageManager imageManager, int[] rgbValues,
                                TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[1]);
        this.getTileIcons()[0] = imageManager.getImage(generateFilename());
    }

    public static String generateFilename(String typeName) {
        return "terrain" + File.separator + typeName + ".png";
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        imageManager.setImage(generateFilename(), this.getTileIcons()[0]);
    }

    private String generateFilename() {
        return generateFilename(this.getTerrainType());
    }

    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}