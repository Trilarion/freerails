package jfreerails.world.track;

import java.util.Iterator;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import jfreerails.world.common.Step;
import jfreerails.world.terrain.TerrainType;

/**
 * Defines methods to access the properties of a track type.
 * 
 * @author Luke Lindsay 09 October 2001
 */
public interface TrackRule extends FreerailsSerializable, Comparable<TrackRule> {

	public enum TrackCategories {
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