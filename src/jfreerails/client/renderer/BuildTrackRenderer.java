package jfreerails.client.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Iterator;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldDifferences;
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

	private final ModelRoot m_modelRoot;

	private final Dimension m_tileSize = new Dimension(30, 30);

	private TrackPieceRendererList m_trackPieceViewList;
	
	public BuildTrackRenderer(TrackPieceRendererList trackPieceViewList, ModelRoot modelRoot) {
		m_modelRoot = modelRoot;
		m_trackPieceViewList = trackPieceViewList;

	}

	private WorldDifferences getWorldDiffs() {
		if (m_modelRoot == null) {
			return null;
		}
		return (WorldDifferences) m_modelRoot
				.getProperty(ModelRoot.Property.PROPOSED_TRACK);
	}

	/**
	 * Paints the proposed track and dots to distinguish the proposed track from
	 * any existing track.
	 */
	public void paint(Graphics2D g) {

		WorldDifferences worldDiffs = getWorldDiffs();	
		if (null != worldDiffs) {
			for (Iterator iter = worldDiffs.getMapDifferences(); iter.hasNext();) {
				Point point = (Point) iter.next();
				TrackPiece tp = (TrackPiece) worldDiffs.getTile(point.x,
						point.y);

				int graphicsNumber = tp.getTrackGraphicID();

				int ruleNumber = tp.getTrackTypeID();
				jfreerails.client.renderer.TrackPieceRenderer trackPieceView = m_trackPieceViewList
						.getTrackPieceView(ruleNumber);
				trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
						point.y, m_tileSize);
			}

			ReadOnlyWorld realWorld = m_modelRoot.getWorld();
			/* Draw small dots for each tile whose track has changed.  The dots
			 * are white if track has been added or upgraded and red if it has been removed.
			 */
			for (Iterator<Point> iter = worldDiffs.getMapDifferences(); iter
					.hasNext();) {
				Point p = iter.next();
				int x = p.x * m_tileSize.width
						+ (m_tileSize.width - SMALL_DOT_WIDTH) / 2;
				int y = p.y * m_tileSize.width
						+ (m_tileSize.height - SMALL_DOT_WIDTH) / 2;
				FreerailsTile before = (FreerailsTile)realWorld.getTile(p.x, p.y);
				FreerailsTile after = (FreerailsTile)worldDiffs.getTile(p.x, p.y);
				
				boolean trackRemoved = !after.getTrackConfiguration().contains(before.getTrackConfiguration());
				Color dotColor = trackRemoved ? Color.RED : Color.WHITE;
				g.setColor(dotColor);
				g.fillOval(x, y, SMALL_DOT_WIDTH, SMALL_DOT_WIDTH);
			}
		}

	}

}