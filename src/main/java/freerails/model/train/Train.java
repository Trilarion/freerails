package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Represents a train.
 */
public class Train extends Identifiable {

    private final int engineId;
    private final List<Integer> wagonTypes;
    private final int cargoBundleId;
    private Schedule schedule;

    /**
     * Makes a copy of the schedule.
     *
     * @param id
     * @param engineId
     * @param wagonTypes
     * @param cargoBundleId
     * @param schedule
     */
    public Train(int id, int engineId, List<Integer> wagonTypes, int cargoBundleId, UnmodifiableSchedule schedule) {
        super(id);
        this.engineId = engineId;
        this.wagonTypes = Collections.unmodifiableList(wagonTypes);
        this.cargoBundleId = cargoBundleId;
        this.schedule = new Schedule(schedule);
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
    public UnmodifiableSchedule getSchedule() {
        return schedule;
    }

    /**
     *
     * @param schedule
     */
    public void setSchedule(@NotNull Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * @return
     */
    public List<Integer> getConsist() {
        return wagonTypes;
    }
}
