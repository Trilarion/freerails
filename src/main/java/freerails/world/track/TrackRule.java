package freerails.world.track;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.Money;
import freerails.world.common.Step;
import freerails.world.terrain.TerrainType;

import java.util.Iterator;

/**
 * Defines methods to access the properties of a track type.
 *
 * @author Luke Lindsay 09 October 2001
 */
public interface TrackRule extends FreerailsSerializable, Comparable<TrackRule> {

    enum TrackCategories {
        track, bridge, tunnel, station, non
    }

    TrackCategories getCategory();

    boolean canBuildOnThisTerrainType(TerrainType.Category TerrainType);

    boolean isStation();

    boolean isDouble();

    Money getPrice();

    Money getFixedCost();

    Money getMaintenanceCost();

    int getStationRadius();

    String getTypeName();

    boolean testTrackPieceLegality(int a9bitTemplate);

    boolean trackPieceIsLegal(TrackConfiguration config);

    int getMaximumConsecutivePieces();

    Step[] getLegalRoutes(Step directionComingFrom);

    Iterator<TrackConfiguration> getLegalConfigurationsIterator();
}