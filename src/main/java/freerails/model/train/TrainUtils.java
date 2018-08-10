package freerails.model.train;

import freerails.model.ModelConstants;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class TrainUtils {

    private TrainUtils() {
    }

    /**
     * @param world
     * @param cargoBatchBundle
     * @param consist
     * @return
     */
    public static List<Integer> spaceAvailable2(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle, List<Integer> consist) {
        // This array will store the amount of space available on the train for each cargo type.
        final int NUM_CARGO_TYPES = world.getCargos().size();
        Integer[] spaceAvailable = new Integer[NUM_CARGO_TYPES];
        Arrays.fill(spaceAvailable, 0);

        // First calculate the train's total capacity.
        for (Integer aConsist : consist) {
            int cargoType = aConsist;
            spaceAvailable[cargoType] += ModelConstants.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            spaceAvailable[cargoType] = spaceAvailable[cargoType] - cargoBatchBundle.getAmountOfType(cargoType);
        }
        // TODO what to do in case of negative numbers? throw an exception?
        return new ArrayList<>(Arrays.asList(spaceAvailable));
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     * @param world
     * @param player
     * @param trainId
     */
    public static Vec2D getTargetLocation(UnmodifiableWorld world, Player player, int trainId) {
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();
        int stationId = schedule.getNextStationId();

        if (-1 == stationId) {
            // There are no stations on the schedule.
            return Vec2D.ZERO;
        }

        Station station = world.getStation(player, stationId);
        return station.location;
    }
}
