package jfreerails.world.track;

import jfreerails.world.common.OneTileMoveVector;


public class LegalRouteAcrossTrackPiece {
    OneTileMoveVector[] a;
    OneTileMoveVector[] b;

    public LegalRouteAcrossTrackPiece(OneTileMoveVector[] aa,
        OneTileMoveVector[] bb) {
        if (aa.length != bb.length) {
            throw new IllegalArgumentException();
        }

        a = (OneTileMoveVector[])aa.clone();
        b = (OneTileMoveVector[])bb.clone();
    }
}