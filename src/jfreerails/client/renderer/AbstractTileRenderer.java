
/*
* TileView.java
*
* Created on 04 July 2001, 07:01
*/
package jfreerails.client.renderer;
import java.awt.Image;
import java.io.File;

import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.World;

/**
*  This class encapsulates the visible properties of a tile.
* @author  Luke Lindsay
*/

public abstract class AbstractTileRenderer implements TileRenderer {

	protected int[] rgbValues;

	protected TileIconSelector tileIconSelector;

	protected Image[] tileIcons;

	protected final TerrainType tileModel;

	protected int rgb;

	protected int tileWidth;

	protected int tileHeight;
	
	public AbstractTileRenderer(TerrainType t){
		tileModel = t;
		if(null == t){
			throw new NullPointerException();
		}
	}

	public void renderTile(
		java.awt.Graphics g,
		int screenX,
		int screenY,
		int mapX,
		int mapY,
		World w) {
		Image icon = this.getIcon(mapX, mapY, w);
		if (null != icon) {
			g.drawImage(icon, screenX, screenY, null);
		}

	}

	public int getRGB() {
		return tileModel.getRGB();
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public Image getIcon() {
		return tileIcons[0];
	}

	public String getTerrainType() {
		return tileModel.getTerrainTypeName();
	}

	public Image getIcon(int x, int y, World w) {
		int tile = selectTileIcon(x, y, w);
		if (tileIcons[tile] != null) {
			return tileIcons[tile];
		} else {
			throw new NullPointerException(
				"Error in TileView.getIcon: icon no. " + tile + "==null");
		}
	}

	/*The terrain types that are treated as the same.  E.g. for terrain type
	river; ocean, ports, and other rivers are treated as the same terrain type.
	*/

	public int selectTileIcon(int x, int y, World w) {
		return 0;
	}

	public void setTileSize(int height, int width) {
		tileHeight = height;
		tileWidth = width;
	}

	protected int checkTile(int x, int y, World w) {
		int match = 0;

		
		if (((x < w.getMapWidth()) && (x >= 0)) && (y < w.getMapHeight()) && (y >= 0)) {
			for (int i = 0; i < rgbValues.length; i++) {
				TerrainTile tt = (TerrainTile) w.getTile(x, y);
				if (tt.terrainRgb() == rgbValues[i]) {
					match = 1;

					//A match
				}
			}
		} else {
			match = 1; //A match

			/*If the tile we are checking is off the map, let it be a match.
			This stops coast appearing where the ocean meets the map edge.
			*/
		}
		return match;
	}

	public void dumpImages(ImageManager imageManager) {
		String relativeFileNameBase = "terrain" + File.separator + this.getTerrainType();
		switch (this.tileIcons.length) {
			case 1 :
				{
					imageManager.setImage(relativeFileNameBase+".png", this.tileIcons[0]);
					break;
				}
			case 2 :
				{
					for (int i = 0; i < this.tileIcons.length; i++) {
						String fileName =
							relativeFileNameBase + "_" + BinaryNumberFormatter.format(i, 1)+".png";
						imageManager.setImage(fileName, this.tileIcons[i]);

					}
					break;
				}
			case 4 :
				{
					for (int i = 0; i < this.tileIcons.length; i++) {
						String fileName =
							relativeFileNameBase + "_" + BinaryNumberFormatter.format(i, 2)+".png";
						imageManager.setImage(fileName, this.tileIcons[i]);

					}
					break;
				}
			case 16 :
				{
					for (int i = 0; i < this.tileIcons.length; i++) {
						String fileName =
							relativeFileNameBase + "_" + BinaryNumberFormatter.format(i, 4)+".png";
						imageManager.setImage(fileName, this.tileIcons[i]);
					}
					break;
				}
			default :
				{
					throw new IllegalArgumentException(String.valueOf(this.tileIcons.length));
				}

		}

	}
}
