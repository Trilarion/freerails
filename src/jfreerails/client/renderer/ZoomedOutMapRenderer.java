package jfreerails.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;


/** This class draws the voerview map.        */
final public class ZoomedOutMapRenderer implements MapRenderer {
    private ReadOnlyWorld w;
    private BufferedImage mapImage;
    protected GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                              .getDefaultScreenDevice()
                                                                              .getDefaultConfiguration();

    public ZoomedOutMapRenderer(ReadOnlyWorld world) {
        this.w = world;
        this.refresh();
    }

    /*
     * @see NewMapView#getScale()
     */
    public float getScale() {
        return 1;
    }

    /*
     * @see NewMapView#paintRect(Graphics, Rectangle)
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.drawImage(mapImage, 0, 0, null);
    }

    /*
     * @see NewMapView#refreshTile(Point)
     */
    public void refreshTile(Point tile) {
        int rgb;

        FreerailsTile tt = w.getTile(tile.x, tile.y);

        if (tt.getTrackPiece().equals(NullTrackPiece.getInstance())) {
            int typeNumber = tt.getTerrainTypeNumber();
            TerrainType terrainType = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                    typeNumber);
            rgb = terrainType.getRGB();
            assert (mapImage != null);
            assert (tile != null);
            mapImage.setRGB(tile.x, tile.y, rgb);
        } else {
            /* black with alpha of 1 */
            mapImage.setRGB(tile.x, tile.y, 0xff000000);
        }
    }

    /*
     * @see NewMapView#refresh()
     */
    public void refresh() {
        int mapWidth = w.getMapWidth();
        int mapHeight = w.getMapHeight();
        mapImage = defaultConfiguration.createCompatibleImage(mapWidth,
                mapHeight);

        Point tile = new Point();

        for (tile.x = 0; tile.x < mapWidth; tile.x++) {
            for (tile.y = 0; tile.y < mapHeight; tile.y++) {
                refreshTile(tile);
            }
        }
    }

    /*
     * @see NewMapView#getMapSizeInPixels()
     */
    public Dimension getMapSizeInPixels() {
        return new Dimension(w.getMapWidth(), w.getMapHeight());
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        g.drawImage(mapImage, 0, 0, null);
    }

    public void refreshTile(int x, int y) {
        refreshTile(new Point(x, y));
    }
}