package jfreerails.world.track;

import jfreerails.world.misc.OneTileMoveVector;
import java.util.Iterator;

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
