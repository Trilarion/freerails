package freerails.client.renderer.track;

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.map.MapLayerRenderer;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackPiece;
import freerails.model.world.ReadOnlyWorld;
import freerails.util.Vec2D;

import java.awt.*;

/**
 * This inner class represents a view of the track on the map.
 */
public final class TrackLayerRenderer implements MapLayerRenderer {

    private final ReadOnlyWorld world;
    private final RendererRoot rendererRoot;
    private final Vec2D mapSize;

    /**
     * @param world
     * @param trackPieceViewList
     */
    public TrackLayerRenderer(ReadOnlyWorld world, RendererRoot trackPieceViewList) {
        rendererRoot = trackPieceViewList;
        this.world = world;
        this.mapSize = world.getMapSize();
    }

    /**
     * Paints a rectangle of tiles onto the supplied graphics context.
     *
     * @param g            The graphics context on which the tiles get painted.
     * @param tilesToPaint The rectangle, measured in tiles, to paint.
     */
    private void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
        /*
         * Track can overlap the adjacent terrain tiles by half a tile. This
         * means that we need to paint the track from the tiles bordering
         * the specified rectangle of tiles (tilesToPaint). To prevent
         * unnecessary painting, we set the clip to expose only the rectangle
         * of tilesToPaint.
         */
        for (int tileX = tilesToPaint.x - 1; tileX < (tilesToPaint.x + tilesToPaint.width + 1); tileX++) {
            for (int tileY = tilesToPaint.y - 1; tileY < (tilesToPaint.y + tilesToPaint.height + 1); tileY++) {
                if ((tileX >= 0) && (tileX < mapSize.x) && (tileY >= 0) && (tileY < mapSize.y)) {
                    Vec2D tileLocation = new Vec2D(tileX, tileY);
                    FullTerrainTile fullTerrainTile = (FullTerrainTile) world.getTile(tileLocation);
                    TrackPiece trackPiece = fullTerrainTile.getTrackPiece();
                    int graphicsNumber = trackPiece.getTrackGraphicID();

                    int ruleNumber = trackPiece.getTrackTypeID();
                    if (ruleNumber != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                        TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);
                        trackPieceView.drawTrackPieceIcon(g, graphicsNumber, tileLocation, ClientConfig.TILE_SIZE);
                    }
                }
            }
        }
    }

    /**
     * @param g
     * @param tileLocation
     */
    public void paintTile(Graphics g, Vec2D tileLocation) {
        /*
         * Since track tiles overlap the adjacent terrain tiles, we create a
         * temporary Graphics object that only lets us draw on the selected
         * tile.
         */
        paintRectangleOfTiles(g, new Rectangle(tileLocation.x, tileLocation.y, 1, 1));
    }

    public void paintRectangleOfTiles(Graphics g, Vec2D p, int width, int height) {
        paintRectangleOfTiles(g, new Rectangle(p.x, p.y, width, height));
    }

    /**
     * @param tileLocation
     */
    public void refreshTile(Vec2D tileLocation) {}

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException("Method not yet implemented.");
    }

    /**
     *
     */
    public void refreshAll() {
    }
}
