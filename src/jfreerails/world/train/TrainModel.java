package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.FreerailsSerializable;


/** Represents a train.
 * @author Luke
 */
public class TrainModel implements FreerailsSerializable {
    public static final int MAX_NUMBER_OF_WAGONS = 6;
    private int scheduleID;
    private int engineType = 0;
    private final int[] wagonTypes;
    private int cargoBundleNumber;

    public int hashCode() {
        int result;
        result = scheduleID;
        result = 29 * result + engineType;
        result = 29 * result + cargoBundleNumber;

        return result;
    }

    public TrainModel getNewInstance(int newEngine, int[] newWagons) {
        return new TrainModel(newEngine, newWagons, this.getScheduleID(),
            this.getCargoBundleNumber());
    }

    /**
     * Constructor for a new train.
     * @param engine type of the engine
     * @param wagons array of wagon types
     * @param p initial position of the train on the map.
     */
    public TrainModel(int engine, int[] wagons, int scheduleID, int BundleId) { //World world){
        this.engineType = engine;
        this.wagonTypes = wagons;

        this.scheduleID = scheduleID;
        this.cargoBundleNumber = BundleId;
    }

    public TrainModel(int[] wagons, int BundleId) {
        this.wagonTypes = wagons;
        this.cargoBundleNumber = BundleId;
    }

    public TrainModel(int engine, int[] wagons, int scheduleID) {
        this.engineType = engine;
        this.wagonTypes = wagons;
        this.scheduleID = scheduleID;
    }

    public TrainModel(int engine) {
        this.engineType = engine;
        wagonTypes = new int[] {0, 1, 2};
    }

    public int getLength() {
        return (1 + wagonTypes.length) * 32; //Engine + wagons.
    }

    public boolean canAddWagon() {
        return wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return wagonTypes.length;
    }

    public int getWagon(int i) {
        return wagonTypes[i];
    }

    //	public void addWagon(int wagonType) {
    //		if (canAddWagon()) {
    //			int oldlength = wagonTypes.length;
    //			int[] newWagons = new int[oldlength + 1];
    //			for (int i = 0; i < oldlength; i++) {
    //				newWagons[i] = wagonTypes[i];
    //			}
    //			newWagons[oldlength] = wagonType;
    //			wagonTypes = newWagons;
    //		} else {
    //			throw new IllegalStateException("Cannot add wagon");
    //		}
    //	}
    public int getEngineType() {
        return engineType;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.cargoBundleNumber == test.cargoBundleNumber &&
                this.engineType == test.engineType &&
                Arrays.equals(this.wagonTypes, test.wagonTypes) &&
                this.scheduleID == test.scheduleID;

            return b;
        } else {
            return false;
        }
    }
}