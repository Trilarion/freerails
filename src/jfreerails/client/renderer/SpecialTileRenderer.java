
/*
* SpecialTileView.java
*
* Created on 20 August 2001, 15:41
*/
package jfreerails.client.renderer;
import jfreerails.world.terrain.TerrainMap;
import jfreerails.world.terrain.TerrainType;

/**
*
* @author  Luke Lindsay
*/

final public class SpecialTileRenderer extends AbstractTileRenderer {

	private TileRenderer parentTileView;

	public SpecialTileRenderer(
		jfreerails.client.common.ImageSplitter imageSplitter,
		int[] rgbValues,
		TerrainType tileModel,
		TileRenderer parentTileView) {
		imageSplitter.setTransparencyToTRANSLUCENT();
		tileIcons = new java.awt.Image[1];
		tileIcons[0] = imageSplitter.getTileFromSubGrid(0, 0);
		this.rgbValues = rgbValues;
		// this.tileModel = tileModel;?????????? TODO fix something here!
		this.parentTileView = parentTileView;
	}

	public void renderTile(
		java.awt.Graphics g,
		int renderX,
		int renderY,
		int mapX,
		int mapY,
		TerrainMap map) {

		if (parentTileView != null) {
			parentTileView.renderTile(g, renderX, renderY, mapX, mapY, map);
		} else {
			System.out.println("parent tileView==null");
		}
		java.awt.Image icon = this.getIcon(mapX, mapX, map);
		if (null != icon) {
			g.drawImage(icon, renderX, renderX, null);

		} else {
			System.out.println("special tileView icon==null");
		}

	}

	public int selectTileIcon(int x, int y, TerrainMap map) {
		return 0;
	}
}