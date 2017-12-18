/*
 * Created on 21-Jul-2003
 *
 */
package freerails.move;

import freerails.world.common.ImPoint;
import freerails.world.track.TrackPiece;

import java.awt.*;

/**
 * This CompositeMove changes the track type at a point on the map and charges
 * the players account for the cost of the change.
 *
 * @author Luke Lindsay
 */
public class UpgradeTrackMove extends CompositeMove implements TrackMove {
    private static final long serialVersionUID = 3907215961470875442L;

    private UpgradeTrackMove(ChangeTrackPieceMove trackMove) {
        super(trackMove);
    }

    /**
     *
     * @param before
     * @param after
     * @param p
     * @return
     */
    public static UpgradeTrackMove generateMove(TrackPiece before,
                                                TrackPiece after, ImPoint p) {
        ChangeTrackPieceMove m = new ChangeTrackPieceMove(before, after, p);

        return new UpgradeTrackMove(m);
    }

    /**
     *
     * @return
     */
    public Rectangle getUpdatedTiles() {
        ChangeTrackPieceMove m = (ChangeTrackPieceMove) this.getMove(0);

        return m.getUpdatedTiles();
    }
}