
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

final public class StandardTileRenderer extends jfreerails.client.renderer.AbstractTileRenderer {

	/** Creates new StandardTileIconSelecter */

	public StandardTileRenderer(
		jfreerails.client.common.ImageSplitter imageSplitter,
		int[] rgbValues,
		TerrainType tileModel) {
		super(tileModel, rgbValues);
		imageSplitter.setTransparencyToOPAQUE();
		tileIcons = new java.awt.Image[1];
		tileIcons[0] = imageSplitter.getTileFromSubGrid(0, 0);
	}
	public StandardTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel)
		throws IOException {
		super(tileModel, rgbValues);
		this.tileIcons = new Image[1];
		this.tileIcons[0] = imageManager.getImage(generateFilename());
	}

	public void dumpImages(ImageManager imageManager) {
		imageManager.setImage(generateFilename(), this.tileIcons[0]);
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
