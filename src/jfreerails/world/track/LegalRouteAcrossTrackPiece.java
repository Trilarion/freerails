package jfreerails.world.track;

import jfreerails.world.common.OneTileMoveVector;


/** The ways a train can travel across a track piece, not implemented yet.
 * @author Luke
 */
public class LegalRouteAcrossTrackPiece {
    private OneTileMoveVector[] a;
    private OneTileMoveVector[] b;

    public LegalRouteAcrossTrackPiece(OneTileMoveVector[] aa,
        OneTileMoveVector[] bb) {
        if (aa.length != bb.length) {
            throw new IllegalArgumentException();
        }

        a = (OneTileMoveVector[])aa.clone();
        b = (OneTileMoveVector[])bb.clone();
    }
}