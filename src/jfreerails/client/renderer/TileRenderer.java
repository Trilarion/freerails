package jfreerails.client.renderer;
import java.awt.Image;

import jfreerails.world.terrain.TerrainMap;

/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/

public interface TileRenderer {

	 int selectTileIcon(int x, int y, TerrainMap map);

	 int getRGB();

	 int getTileWidth();

	 int getTileHeight();

	 Image getIcon(int x, int y, TerrainMap map);

	 Image getIcon();

	 void renderTile(
		java.awt.Graphics g,
		int renderX,
		int renderY,
		int mapX,
		int mapY,
		TerrainMap map);

	 String getTerrainType();
}