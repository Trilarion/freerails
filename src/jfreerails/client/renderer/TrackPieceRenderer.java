package jfreerails.client.renderer;
import java.awt.Image;

/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/

public interface TrackPieceRenderer {

	 Image getTrackPieceIcon(int trackTemplate);

	 void drawTrackPieceIcon(
		int trackTemplate,
		java.awt.Graphics g,
		int x,
		int y,
		java.awt.Dimension tileSize);
}