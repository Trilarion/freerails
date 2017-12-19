package freerails.client.renderer;

import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.client.Constants;
import freerails.world.common.ImPoint;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.WorldDiffs;
import freerails.world.track.FreerailsTile;
import freerails.world.track.TrackPiece;

import java.awt.*;
import java.util.Iterator;

/**
 * This class draws the track being build.
 *
 */
public class BuildTrackRenderer implements Painter {

    /**
     *
     */
    public static final int BIG_DOT_WIDTH = 12;

    /**
     *
     */
    public static final int SMALL_DOT_WIDTH = 6;

    private final ModelRoot modelRoot;

    private final Dimension tileSize = new Dimension(Constants.TILE_SIZE,
            Constants.TILE_SIZE);

    private final RenderersRoot rr;

    /**
     *
     * @param trackPieceViewList
     * @param modelRoot
     */
    public BuildTrackRenderer(RenderersRoot trackPieceViewList,
                              ModelRoot modelRoot) {
        this.modelRoot = modelRoot;
        this.rr = trackPieceViewList;

    }

    private WorldDiffs getWorldDiffs() {
        if (modelRoot == null) {
            return null;
        }
        return (WorldDiffs) modelRoot
                .getProperty(ModelRoot.Property.PROPOSED_TRACK);
    }

    /**
     * Paints the proposed track and dots to distinguish the proposed track from
     * any existing track.
     * @param g
     * @param newVisibleRectectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectectangle) {

        WorldDiffs worldDiffs = getWorldDiffs();
        if (null != worldDiffs) {
            for (Iterator<ImPoint> iter = worldDiffs.getMapDiffs(); iter
                    .hasNext(); ) {
                ImPoint point = iter.next();
                FreerailsTile fp = (FreerailsTile) worldDiffs.getTile(point.x,
                        point.y);
                TrackPiece tp = fp.getTrackPiece();

                int graphicsNumber = tp.getTrackGraphicID();

                int ruleNumber = tp.getTrackTypeID();
                freerails.client.renderer.TrackPieceRenderer trackPieceView = rr
                        .getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
                        point.y, tileSize);
            }

            ReadOnlyWorld realWorld = modelRoot.getWorld();
            /*
             * Draw small dots for each tile whose track has changed. The dots
             * are white if track has been added or upgraded and red if it has
             * been removed.
             */
            for (Iterator<ImPoint> iter = worldDiffs.getMapDiffs(); iter
                    .hasNext(); ) {
                ImPoint p = iter.next();
                int x = p.x * tileSize.width
                        + (tileSize.width - SMALL_DOT_WIDTH) / 2;
                int y = p.y * tileSize.width
                        + (tileSize.height - SMALL_DOT_WIDTH) / 2;
                FreerailsTile before = (FreerailsTile) realWorld.getTile(p.x,
                        p.y);
                FreerailsTile after = (FreerailsTile) worldDiffs.getTile(p.x,
                        p.y);

                boolean trackRemoved = !after.getTrackPiece()
                        .getTrackConfiguration().contains(
                                before.getTrackPiece().getTrackConfiguration());
                Color dotColor = trackRemoved ? Color.RED : Color.WHITE;
                g.setColor(dotColor);
                g.fillOval(x, y, SMALL_DOT_WIDTH, SMALL_DOT_WIDTH);
            }
        }

    }

}