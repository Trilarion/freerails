package jfreerails.client.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Iterator;

import jfreerails.client.common.Painter;
import jfreerails.controller.ModelRoot;
import jfreerails.world.common.ImPoint;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;

/**
 * This class draws the track being build.
 * 
 * @author MystiqueAgent
 * @author Luke
 * 
 */
public class BuildTrackRenderer implements Painter {
	public static final int BIG_DOT_WIDTH = 12;

	public static final int SMALL_DOT_WIDTH = 6;

	private final ModelRoot modelRoot;

	private final Dimension tileSize = new Dimension(30, 30);

	private TrackPieceRendererList trackPieceViewList;

	public BuildTrackRenderer(TrackPieceRendererList trackPieceViewList,
			ModelRoot modelRoot) {
		this.modelRoot = modelRoot;
		this.trackPieceViewList = trackPieceViewList;

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
	 */
	public void paint(Graphics2D g) {

		WorldDiffs worldDiffs = getWorldDiffs();
		if (null != worldDiffs) {
			for (Iterator<ImPoint> iter = worldDiffs.getMapDiffs(); iter
					.hasNext();) {
				ImPoint point = iter.next();
				FreerailsTile fp = (FreerailsTile)worldDiffs.getTile(point.x,
						point.y);
				TrackPiece tp = fp.getTrackPiece();

				int graphicsNumber = tp.getTrackGraphicID();

				int ruleNumber = tp.getTrackTypeID();
				jfreerails.client.renderer.TrackPieceRenderer trackPieceView = trackPieceViewList
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
					.hasNext();) {
				ImPoint p = iter.next();
				int x = p.x * tileSize.width
						+ (tileSize.width - SMALL_DOT_WIDTH) / 2;
				int y = p.y * tileSize.width
						+ (tileSize.height - SMALL_DOT_WIDTH) / 2;
				FreerailsTile before = (FreerailsTile) realWorld.getTile(p.x,
						p.y);
				FreerailsTile after = (FreerailsTile) worldDiffs.getTile(p.x,
						p.y);

				boolean trackRemoved = !after.getTrackPiece().getTrackConfiguration().contains(
						before.getTrackPiece().getTrackConfiguration());
				Color dotColor = trackRemoved ? Color.RED : Color.WHITE;
				g.setColor(dotColor);
				g.fillOval(x, y, SMALL_DOT_WIDTH, SMALL_DOT_WIDTH);
			}
		}

	}

}