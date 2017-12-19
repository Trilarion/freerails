package freerails.world.track;

import freerails.world.FreerailsSerializable;
import freerails.world.common.Money;
import freerails.world.common.Step;
import freerails.world.terrain.TerrainType;

import java.util.Iterator;

/**
 * Defines methods to access the properties of a track type.
 *
 */
public interface TrackRule extends FreerailsSerializable, Comparable<TrackRule> {

    /**
     *
     * @return
     */
    TrackCategories getCategory();

    /**
     *
     * @param TerrainType
     * @return
     */
    boolean canBuildOnThisTerrainType(TerrainType.Category TerrainType);

    /**
     *
     * @return
     */
    boolean isStation();

    /**
     *
     * @return
     */
    boolean isDouble();

    /**
     *
     * @return
     */
    Money getPrice();

    /**
     *
     * @return
     */
    Money getFixedCost();

    /**
     *
     * @return
     */
    Money getMaintenanceCost();

    /**
     *
     * @return
     */
    int getStationRadius();

    /**
     *
     * @return
     */
    String getTypeName();

    /**
     *
     * @param a9bitTemplate
     * @return
     */
    boolean testTrackPieceLegality(int a9bitTemplate);

    /**
     *
     * @param config
     * @return
     */
    boolean trackPieceIsLegal(TrackConfiguration config);

    /**
     *
     * @return
     */
    int getMaximumConsecutivePieces();

    /**
     *
     * @param directionComingFrom
     * @return
     */
    Step[] getLegalRoutes(Step directionComingFrom);

    /**
     *
     * @return
     */
    Iterator<TrackConfiguration> getLegalConfigurationsIterator();

    /**
     *
     */
    enum TrackCategories {

        /**
         *
         */
        track,

        /**
         *
         */
        bridge,

        /**
         *
         */
        tunnel,

        /**
         *
         */
        station,

        /**
         *
         */
        non
    }
}