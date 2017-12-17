/*
 * TileView.java
 *
 * Created on 04 July 2001, 07:01
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainTile;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.File;

/**
 * This class encapsulates the visible properties of a tile.
 *
 * @author Luke Lindsay
 */
public abstract class AbstractTileRenderer implements TileRenderer {
    private final int[] typeNumbers;

    private Image[] tileIcons;

    private final int mapWidth;
    private final int mapHeight;

    private final TerrainType tileModel;

    AbstractTileRenderer(TerrainType t, int[] rgbValues, ReadOnlyWorld w) {
        mapWidth = w.getMapWidth();
        mapHeight = w.getMapHeight();

        tileModel = t;
        this.typeNumbers = rgbValues;

        if (null == t) {
            throw new NullPointerException();
        }

        if (null == rgbValues) {
            throw new NullPointerException();
        }
    }

    public void renderTile(java.awt.Graphics g, int screenX, int screenY,
                           int mapX, int mapY, ReadOnlyWorld w) {
        Image icon = this.getIcon(mapX, mapY, w);

        if (null != icon) {
            g.drawImage(icon, screenX, screenY, null);
        }
    }

    public Image getDefaultIcon() {
        return getTileIcons()[0];
    }

    String getTerrainType() {
        return tileModel.getTerrainTypeName();
    }

    /**
     * Returns an icon for the tile at x,y, which may depend on the terrain
     * types of of the surrounding tiles.
     */
    Image getIcon(int x, int y, ReadOnlyWorld w) {
        int tile = selectTileIcon(x, y, w);

        if (getTileIcons()[tile] != null) {
            return getTileIcons()[tile];
        }
        throw new NullPointerException("Error in TileView.getIcon: icon no. "
                + tile + "==null");
    }

    int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    // 666 remove wo !
    int checkTile(int x, int y, ReadOnlyWorld w) {
        int match = 0;

        if ((x < mapWidth) && (x >= 0) && (y < mapHeight) && (y >= 0)) {
            for (int typeNumber : typeNumbers) {
                TerrainTile tt = (TerrainTile) w.getTile(x, y);

                if (tt.getTerrainTypeID() == typeNumber) {
                    match = 1;
                    // A match
                }
            }
        } else {
            match = 1; // A match
            /*
             * If the tile we are checking is off the map, let it be a match.
             * This stops coast appearing where the ocean meets the map edge.
             */
        }
        return match;
    }

    abstract public void dumpImages(ImageManager imageManager);

    String generateRelativeFileName(int i) {
        return "terrain" + File.separator + this.getTerrainType() + "_"
                + generateFileNameNumber(i) + ".png";
    }

    protected abstract String generateFileNameNumber(int i);

    void setTileIcons(Image[] tileIcons) {
        this.tileIcons = tileIcons;
    }

    Image[] getTileIcons() {
        return tileIcons;
    }
}