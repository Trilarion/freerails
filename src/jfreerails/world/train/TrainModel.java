package jfreerails.world.train;

import java.util.Arrays;
import jfreerails.world.common.FreerailsSerializable;


/** Represents a train.
 * @author Luke
 */
public class TrainModel implements FreerailsSerializable {
    public static final int MAX_NUMBER_OF_WAGONS = 6;
    private final int m_scheduleID;
    private final int m_engineType;
    private final int[] m_wagonTypes;
    private final int m_cargoBundleNumber;

    public int hashCode() {
        int result;
        result = m_scheduleID;
        result = 29 * result + m_engineType;
        result = 29 * result + m_cargoBundleNumber;

        return result;
    }

    public TrainModel getNewInstance(int newEngine, /*=const*/
        int[] newWagons) {
        return new TrainModel(newEngine, newWagons, this.getScheduleID(),
            this.getCargoBundleNumber());
    }

    public TrainModel(int engine, /*=const*/
        int[] wagons, int scheduleID, int BundleId) {
        m_engineType = engine;
        m_wagonTypes = wagons;
        m_scheduleID = scheduleID;
        m_cargoBundleNumber = BundleId;
    }

    public TrainModel( /*=const*/
        int[] wagons, int BundleId) {
        m_wagonTypes = wagons;
        m_cargoBundleNumber = BundleId;
        m_engineType = 0;
        m_scheduleID = 0;
    }

    public TrainModel(int engine, /*=const*/
        int[] wagons, int scheduleID) {
        m_engineType = engine;
        m_wagonTypes = wagons;
        m_scheduleID = scheduleID;
        m_cargoBundleNumber = 0;
    }

    public TrainModel(int engine) {
        m_engineType = engine;
        m_wagonTypes = new int[] {0, 1, 2};
        m_scheduleID = 0;
        m_cargoBundleNumber = 0;
    }

    public int getLength() {
        return (1 + m_wagonTypes.length) * 32; //Engine + wagons.
    }

    public boolean canAddWagon() {
        return m_wagonTypes.length < MAX_NUMBER_OF_WAGONS;
    }

    public int getNumberOfWagons() {
        return m_wagonTypes.length;
    }

    public int getWagon(int i) {
        return m_wagonTypes[i];
    }

    public int getEngineType() {
        return m_engineType;
    }

    public int getCargoBundleNumber() {
        return m_cargoBundleNumber;
    }

    public int getScheduleID() {
        return m_scheduleID;
    }

    public int[] getConsist() {
        return (int[])m_wagonTypes.clone();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TrainModel) {
            TrainModel test = (TrainModel)obj;
            boolean b = this.m_cargoBundleNumber == test.m_cargoBundleNumber &&
                this.m_engineType == test.m_engineType &&
                Arrays.equals(this.m_wagonTypes, test.m_wagonTypes) &&
                this.m_scheduleID == test.m_scheduleID;

            return b;
        } else {
            return false;
        }
    }
}