package jfreerails.world.track;

import jfreerails.world.misc.OneTileMoveVector;

public class LegalRouteAcrossTrackPiece {

	OneTileMoveVector[] a, b;
	
	public LegalRouteAcrossTrackPiece(OneTileMoveVector[] aa, OneTileMoveVector[] bb){
		if(aa.length!=bb.length){
			throw new IllegalArgumentException();
		}
		a=(OneTileMoveVector[])aa.clone();
		b=(OneTileMoveVector[])bb.clone();			
	}
}
