/*
 * Created on 28-Apr-2003
 *
 */
package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import jfreerails.client.common.ImageManager;
import jfreerails.client.renderer.TileRenderer;
import jfreerails.client.renderer.TileRendererList;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * Simple implementation of TileRendererList, for testing purposes only.
 *
 * @author Luke
 *
 */
public class QuickRGBTileRendererList implements TileRendererList {
    private int[] rgbValues;
    private Image[] images;
    private HashMap rgb2index = new HashMap();
    private SimpleTileRenderer simpleTileRenderer = new SimpleTileRenderer();
    private static java.awt.GraphicsConfiguration defaultConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                                     .getDefaultScreenDevice()
                                                                                                     .getDefaultConfiguration();

    public QuickRGBTileRendererList(ReadOnlyWorld w) {
        int numberOfTerrainTypes = w.size(KEY.TERRAIN_TYPES);
        rgbValues = new int[numberOfTerrainTypes];
        images = new Image[numberOfTerrainTypes];

        for (int i = 0; i < numberOfTerrainTypes; i++) {
            TerrainType t = (TerrainType)w.get(KEY.TERRAIN_TYPES, i);
            rgbValues[i] = t.getRGB();
            images[i] = createImageFor(t);
            rgb2index.put(new Integer(t.getRGB()), new Integer(i));
        }
    }

    public static Image createImageFor(TerrainType t) {
	System.out.println("Creating QuickRGBImage");
        Image image = defaultConfiguration.createCompatibleImage(30, 30);
        Color c = new Color(t.getRGB());
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        return image;
    }

    public TileRenderer getTileViewWithNumber(int i) {
        throw new UnsupportedOperationException();
    }

    public TileRenderer getTileViewWithRGBValue(int rgb) {
        Integer i = (Integer)rgb2index.get(new Integer(rgb));
        this.simpleTileRenderer.setImage(images[i.intValue()]);

        return simpleTileRenderer;
    }

    public boolean validate(ReadOnlyWorld world) {
        return true;
    }

    class SimpleTileRenderer implements TileRenderer {
        Image i;

        public SimpleTileRenderer() {
        }

        public void setImage(Image i) {
            this.i = i;
        }

        public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
            return 0;
        }

        public int getTileWidth() {
            return 30;
        }

        public int getTileHeight() {
            return 30;
        }

        public Image getIcon(int x, int y, ReadOnlyWorld w) {
            return i;
        }

        public Image getDefaultIcon() {
            return i;
        }

        public void renderTile(Graphics g, int renderX, int renderY, int mapX,
            int mapY, ReadOnlyWorld w) {
            g.drawImage(i, renderX, renderY, null);
        }

        public void dumpImages(ImageManager imageManager) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }
    }
}
