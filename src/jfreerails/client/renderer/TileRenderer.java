package jfreerails.client.renderer;
import java.awt.Image;

import jfreerails.client.common.ImageManager;
import jfreerails.world.top.World;


/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/

public interface TileRenderer {

	 int selectTileIcon(int x, int y, World w);

	 int getRGB();

	 int getTileWidth();

	 int getTileHeight();

	 Image getIcon(int x, int y, World w);

	 Image getIcon();

	 void renderTile(
		java.awt.Graphics g,
		int renderX,
		int renderY,
		int mapX,
		int mapY,
		World w);

	 String getTerrainType();
	 
	 /** Adds the images this TileRenderer uses to the specified ImageManager. */
	 void dumpImages(ImageManager imageManager);
}