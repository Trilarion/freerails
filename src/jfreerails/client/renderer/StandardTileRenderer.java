/*
* StandardTileIconSelecter.java
*
* Created on 07 July 2001, 12:11
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainType;


/**
*
* @author  Luke Lindsay
*/
final public class StandardTileRenderer
    extends jfreerails.client.renderer.AbstractTileRenderer {
    public StandardTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.setTileIcons(new Image[1]);
        this.getTileIcons()[0] = imageManager.getImage(generateFilename());
    }

    public void dumpImages(ImageManager imageManager) {
        imageManager.setImage(generateFilename(), this.getTileIcons()[0]);
    }

    private String generateFilename() {
        return generateFilename(this.getTerrainType());
    }

    public static String generateFilename(String typeName) {
        return "terrain" + File.separator + typeName + ".png";
    }

    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}