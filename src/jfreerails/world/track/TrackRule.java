package jfreerails.world.track;

import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import jfreerails.world.common.OneTileMoveVector;


/**
* Defines methods to access the properties of a track type.
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TrackRule extends FreerailsSerializable {
    boolean canBuildOnThisTerrainType(String TerrainType);

    boolean isStation();

    Money getPrice();

    Money getMaintenanceCost();

    int getStationRadius();

    int getRuleNumber();

    String getTypeName();

    boolean testTrackPieceLegality(int trackTemplateToTest);

    boolean trackPieceIsLegal(TrackConfiguration config);

    int getMaximumConsecutivePieces();

    OneTileMoveVector[] getLegalRoutes(OneTileMoveVector directionComingFrom);

    Iterator getLegalConfigurationsIterator();
}