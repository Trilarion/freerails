
/*
* SpecialTileView.java
*
* Created on 20 August 2001, 15:41
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageSplitter;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;

/**
*
* @author  Luke Lindsay
*/

final public class SpecialTileRenderer extends AbstractTileRenderer {

	private TileRenderer parentTileView;

	public SpecialTileRenderer(
		ImageSplitter imageSplitter,
		int[] rgbValues,
		TerrainType tileModel,
		TileRenderer parentTileView) {
		super(tileModel, rgbValues);
		imageSplitter.setTransparencyToTRANSLUCENT();
		tileIcons = new java.awt.Image[1];
		tileIcons[0] = imageSplitter.getTileFromSubGrid(0, 0);				
		this.parentTileView = parentTileView;
	}

	public void renderTile(
		java.awt.Graphics g,
		int renderX,
		int renderY,
		int mapX,
		int mapY,
		ReadOnlyWorld w) {

		if (parentTileView != null) {
			parentTileView.renderTile(g, renderX, renderY, mapX, mapY, w);
		} else {
			System.err.println("parent tileView==null");
		}
		java.awt.Image icon = this.getIcon(mapX, mapX, w);
		if (null != icon) {
			g.drawImage(icon, renderX, renderX, null);

		} else {
			System.err.println("special tileView icon==null");
		}

	}

	public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
		return 0;
	}

	public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel)
		throws IOException {
		super(tileModel, rgbValues);
		this.tileIcons = new Image[1];
		this.tileIcons[0] = imageManager.getImage(generateFilename());
	}

	public void dumpImages(ImageManager imageManager) {
		imageManager.setImage(generateFilename(), this.tileIcons[0]);
	}
	private String generateFilename() {
		return "terrain" + File.separator + this.getTerrainType() + ".png";
	}

	protected String generateFileNameNumber(int i) {
		throw new UnsupportedOperationException();
	}
}