
/*
* ForestStyleTileView.java
*
* Created on 07 July 2001, 14:36
*/
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;

import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageSplitter;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;

/**
*
* @author  Luke Lindsay
*/

final public class ForestStyleTileRenderer
	extends jfreerails.client.renderer.AbstractTileRenderer {

	private static final int[] X_LOOK_AT = { -1, 1 };

	private static final int[] Y_LOOK_AT = { 0, 0 };

	/** Creates new ForestStyleTileView */

	public ForestStyleTileRenderer(
		ImageSplitter imageSplitter,
		int[] rgbValues,
		TerrainType tileModel) {
		super(tileModel, rgbValues);
		imageSplitter.setTransparencyToOPAQUE();
		tileIcons = new java.awt.Image[4];

		//Grap them in this order so that they display correctly :)
		tileIcons[0] = imageSplitter.getTileFromSubGrid(0, 0);
		tileIcons[1] = imageSplitter.getTileFromSubGrid(1, 0);
		tileIcons[2] = imageSplitter.getTileFromSubGrid(3, 0);
		tileIcons[3] = imageSplitter.getTileFromSubGrid(2, 0);
	}

	public ForestStyleTileRenderer(
		ImageManager imageManager,
		int[] rgbValues,
		TerrainType tileModel)
		throws IOException {
		super(tileModel, rgbValues);
		this.tileIcons = new Image[4];
		for (int i = 0; i < this.tileIcons.length; i++) {
			String fileName = generateRelativeFileName(i);
			this.tileIcons[i] = imageManager.getImage(fileName);
		}

	}

	public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
		int iconNumber = 0;
		for (int i = 0; i < 2; i++) {
			iconNumber = iconNumber | checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
			iconNumber = iconNumber << 1;
		}
		iconNumber = iconNumber >> 1;
		return iconNumber;
	}

	public void dumpImages(ImageManager imageManager) {
		for (int i = 0; i < this.tileIcons.length; i++) {
			String fileName = generateRelativeFileName(i);
			imageManager.setImage(fileName, this.tileIcons[i]);
		}
	}

	protected String generateFileNameNumber(int i) {
		return BinaryNumberFormatter.format(i, 2);
	}
}
