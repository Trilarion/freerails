package jfreerails.world.track;

import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;


/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TrackRule extends FreerailsSerializable {
    boolean canBuildOnThisTerrainType(String TerrainType);

    boolean isStation();

    long getPrice();

    long getMaintenanceCost();

    int getStationRadius();

    int getRuleNumber();

    String getTypeName();

    boolean testTrackPieceLegality(int trackTemplateToTest);

    boolean trackPieceIsLegal(TrackConfiguration config);

    int getMaximumConsecutivePieces();

    jfreerails.world.common.OneTileMoveVector[] getLegalRoutes(
        jfreerails.world.common.OneTileMoveVector directionComingFrom);

    Iterator getLegalConfigurationsIterator();

    TrackPiece getTrackPiece(TrackConfiguration config);
}
