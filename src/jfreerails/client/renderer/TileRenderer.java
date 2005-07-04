package jfreerails.client.renderer;

import java.awt.Image;

import jfreerails.client.common.ImageManager;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Draws an icon to represent a tile.
 * 
 * @author Luke Lindsay
 * 
 */
public interface TileRenderer {
	Image getDefaultIcon();

	void renderTile(java.awt.Graphics g, int renderX, int renderY, int mapX,
			int mapY, ReadOnlyWorld w);

	/** Adds the images this TileRenderer uses to the specified ImageManager. */
	void dumpImages(ImageManager imageManager);
}