package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;

/**
 * Represents a train.
 * 
 * @author Luke
 */
public class TrainModel implements FreerailsSerializable {
    public static final int WAGON_LENGTH = 24;

    private static final long serialVersionUID = 3545235825756812339L;

    public static final int MAX_NUMBER_OF_WAGONS = 6;

    public static final int MAX_TRAIN_LENGTH = (1 + MAX_NUMBER_OF_WAGONS)
            * WAGON_LENGTH;

    private final int scheduleId;

    private final int engineTypeId;

    private final ImInts wagonTypes;

    private final int cargoBundleId;

    @Override
    public int hashCode() {
        int result;
        result = scheduleId;
        result = 29 * result + engineTypeId;
        result = 29 * result + cargoBundleId;

        return result;
    }

    public TrainModel getNewInstance(int newEngine, ImInts newWagons) {
        return new TrainModel(newEngine, newWagons, this.getScheduleID(), this
                .getCargoBundleID());
    }

    public TrainModel(int engine, ImInts wagons, int scheduleID, int BundleId) {
        engineTypeId = engine;
        wagonTypes = wagons;
        scheduleId = scheduleID;
        cargoBundleId = BundleId;
    }

    public TrainModel(ImInts wagons, int BundleId) {
        wagonTypes = wagons;
        cargoBundleId = BundleId;
        engineTypeId = 0;
        scheduleId = 0;
    }

    public TrainModel(int engine, ImInts wagons, int scheduleID) {
        engineTypeId = engine;
        wagonTypes = wagons;
        scheduleId = scheduleID;
        cargoBundleId = 0;
    }

    public TrainModel(int engine) {
        engineTypeId = engine;
        wagonTypes = new ImInts(0, 1, 2);
        scheduleId = 0;
        cargoBundleId = 0;
    }

    public int getLength() {
        return (1 + wagonTypes.size()) * WAGON_LENGTH; // Engine + wagons.
    }

    public boolean canAddWagon() {
        return wagonTypes.size() < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    public int getWagon(int i) {
        return wagonTypes.get(i);
    }

    public int getEngineType() {
        return engineTypeId;
    }

    public int getCargoBundleID() {
        return cargoBundleId;
    }

    public int getScheduleID() {
        return scheduleId;
    }

    public ImInts getConsist() {
        return wagonTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel) obj;
            boolean b = this.cargoBundleId == test.cargoBundleId
                    && this.engineTypeId == test.engineTypeId
                    && this.wagonTypes.equals(test.wagonTypes)
                    && this.scheduleId == test.scheduleId;

            return b;
        }
        return false;
    }
}