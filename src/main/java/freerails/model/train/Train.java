package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.ModelConstants;

import java.util.Collections;
import java.util.List;

/**
 * Represents a train.
 */
public class Train extends Identifiable {

    private final int engineId;
    private final List<Integer> wagonTypes;
    private final int cargoBundleId;
    private final int scheduleId;

    public Train(int id, int engineId, List<Integer> wagonTypes, int cargoBundleId, int scheduleId) {
        super(id);
        this.engineId = engineId;
        this.wagonTypes = Collections.unmodifiableList(wagonTypes);
        this.cargoBundleId = cargoBundleId;
        this.scheduleId = scheduleId;
    }

    /**
     * @return
     */
    public int getLength() {
        return (1 + wagonTypes.size()) * ModelConstants.WAGON_LENGTH; // Engine + wagons.
    }

    /**
     * @return
     */
    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    /**
     * @param position
     * @return
     */
    public int getWagonType(int position) {
        return wagonTypes.get(position);
    }

    /**
     * @return
     */
    public int getEngineId() {
        return engineId;
    }

    /**
     * @return
     */
    public int getCargoBundleId() {
        return cargoBundleId;
    }

    /**
     * @return
     */
    public int getScheduleId() {
        return scheduleId;
    }

    /**
     * @return
     */
    public List<Integer> getConsist() {
        return wagonTypes;
    }
}
