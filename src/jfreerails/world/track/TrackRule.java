package jfreerails.world.track;
import java.util.Iterator;

import jfreerails.world.misc.FreerailsSerializable;

/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/

public interface TrackRule extends FreerailsSerializable {

	boolean canBuildOnThisTerrainType(String TerrainType);
	
	boolean isStation();

	int getRuleNumber();

	String getTypeName();

	boolean testTrackPieceLegality(int trackTemplateToTest);

	boolean trackPieceIsLegal(TrackConfiguration config);

	int getMaximumConsecutivePieces();

	jfreerails.world.misc.OneTileMoveVector[] getLegalRoutes(
		jfreerails.world.misc.OneTileMoveVector directionComingFrom);

	boolean isDoubleTrackEnabled();

	Iterator getLegalConfigurationsIterator();

	TrackPiece getTrackPiece(TrackConfiguration config);

}