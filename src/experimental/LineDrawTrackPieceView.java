
/*
* LineDrawTrackPieceView.java
*
* Created on 09 October 2001, 23:53
*/
package experimental;

import java.awt.Graphics2D;

/**
*
* @author  Luke Lindsay
*/


public class LineDrawTrackPieceView implements jfreerails.client.renderer.TrackPieceRenderer {

    int[] xx =  {
        -1, 0, 1, -1, 0, 1, -1, 0, 1
    };

    int[] yy =  {
        -1, -1, -1, 0, 0, 0, 1, 1, 1
    };

    /**
    * Gets the trackPieceIcon attribute of the TrackPieceViewInterface object
    *
    * @param  trackTemplate           Description of Parameter
    * @return                         The trackPieceIcon value
    * @exception  FreerailsException  Description of Exception
    */

    public java.awt.Image getTrackPieceIcon( int trackTemplate )  {
        return null;
    }

    /** Creates new LineDrawTrackPieceView */

    public LineDrawTrackPieceView() {

    }

    /**
    * Description of the Method
    *
    * @param  trackTemplate           Description of Parameter
    * @param  g                       Description of Parameter
    * @param  x                       Description of Parameter
    * @param  y                       Description of Parameter
    * @param  tileSize                Description of Parameter
    * @exception  FreerailsException  Description of Exception
    */

    public void drawTrackPieceIcon( int trackTemplate, java.awt.Graphics g, int x, int y, java.awt.Dimension tileSize )  {
        Graphics2D  g2 = (Graphics2D)g;
        g2.setStroke( new java.awt.BasicStroke( 8.0f ) );
        g2.setColor( java.awt.Color.red );
        if( 0 != trackTemplate ) {
            int  drawX = x * tileSize.width;
            int  drawY = y * tileSize.height;

            //g.drawLine(drawX-10,drawY-10,drawX+10,drawY+10);
            for( int  i = 0;i < 9;i++ ) {
                if( ( trackTemplate & ( 1 << i ) ) == ( 1 << i ) ) {
                    g2.drawLine( drawX + 15, drawY + 15, drawX + 15 + 15 * xx[ i ], drawY + 15 + 15 * yy[ i ] );
                }
            }
        }
    }
}
