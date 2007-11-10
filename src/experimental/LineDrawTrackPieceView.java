/*
 * LineDrawTrackPieceView.java
 *
 * Created on 09 October 2001, 23:53
 */
package experimental;

import java.awt.Graphics2D;

import jfreerails.client.common.ImageManager;

/**
 * This TrackPieceRenderer renders track pieces by drawing lines so avoids the
 * need to load images.
 * 
 * @author Luke Lindsay
 */
public class LineDrawTrackPieceView implements
        jfreerails.client.renderer.TrackPieceRenderer {
    private int[] xx = { -1, 0, 1, -1, 0, 1, -1, 0, 1 };

    private int[] yy = { -1, -1, -1, 0, 0, 0, 1, 1, 1 };

    public java.awt.Image getTrackPieceIcon(int trackTemplate) {
        return null;
    }

    public void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g,
            int x, int y, java.awt.Dimension tileSize) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new java.awt.BasicStroke(8.0f));
        g2.setColor(java.awt.Color.red);

        if (0 != trackTemplate) {
            int drawX = x * tileSize.width;
            int drawY = y * tileSize.height;

            // g.drawLine(drawX-10,drawY-10,drawX+10,drawY+10);
            for (int i = 0; i < 9; i++) {
                if ((trackTemplate & (1 << i)) == (1 << i)) {
                    g2.drawLine(drawX + 15, drawY + 15,
                            drawX + 15 + 15 * xx[i], drawY + 15 + 15 * yy[i]);
                }
            }
        }
    }

    public void dumpImages(ImageManager imageManager) {
        // TODO Auto-generated method stub
    }
}