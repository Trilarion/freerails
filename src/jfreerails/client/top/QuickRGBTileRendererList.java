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
import jfreerails.world.Constants;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;

/**
 * Simple implementation of TileRendererList, for testing purposes only.
 * 
 * @author Luke
 * 
 */
public class QuickRGBTileRendererList implements TileRendererList {
	private final int[] rgbValues;

	private final Image[] images;

	private final HashMap<Integer, Integer> rgb2index = new HashMap<Integer, Integer>();

	private final SimpleTileRenderer simpleTileRenderer = new SimpleTileRenderer();

	private static final java.awt.GraphicsConfiguration defaultConfiguration = java.awt.GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration();

	public QuickRGBTileRendererList(ReadOnlyWorld w) {
		int numberOfTerrainTypes = w.size(SKEY.TERRAIN_TYPES);
		rgbValues = new int[numberOfTerrainTypes];
		images = new Image[numberOfTerrainTypes];

		for (int i = 0; i < numberOfTerrainTypes; i++) {
			TerrainType t = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
			rgbValues[i] = t.getRGB();
			images[i] = createImageFor(t);
			rgb2index.put(new Integer(t.getRGB()), new Integer(i));
		}
	}

	public static Image createImageFor(TerrainType t) {
        Image image = defaultConfiguration.createCompatibleImage(
                Constants.TILE_SIZE, Constants.TILE_SIZE);
		Color c = new Color(t.getRGB());
		Graphics g = image.getGraphics();
		g.setColor(c);
        g.fillRect(0, 0, Constants.TILE_SIZE, Constants.TILE_SIZE);
		g.dispose();

		return image;
	}

	public TileRenderer getTileViewWithNumber(int i) {
		throw new UnsupportedOperationException();
	}

	public TileRenderer getTileViewWithRGBValue(int rgb) {
		Integer i = rgb2index.get(new Integer(rgb));
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