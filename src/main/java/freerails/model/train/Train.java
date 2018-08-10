package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
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
    private CargoBatchBundle cargoBatchBundle;
    private Schedule schedule;

    /**
     * Makes a copy of the schedule.
     *
     * @param id
     * @param engineId
     * @param wagonTypes
     * @param cargoBatchBundle
     * @param schedule
     */
    public Train(int id, int engineId, List<Integer> wagonTypes, @NotNull UnmodifiableCargoBatchBundle cargoBatchBundle, @NotNull UnmodifiableSchedule schedule) {
        super(id);
        this.engineId = engineId;
        this.wagonTypes = Collections.unmodifiableList(wagonTypes);
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
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
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /**
     * Makes a copy.
     *
     * @param cargoBatchBundle
     */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
    }

    /**
     * @return
     */
    public UnmodifiableSchedule getSchedule() {
        return schedule;
    }

    /**
     * Makes a copy of the schedule.
     * @param schedule
     */
    public void setSchedule(@NotNull UnmodifiableSchedule schedule) {
        this.schedule = new Schedule(schedule);
    }

    /**
     * @return
     */
    public List<Integer> getConsist() {
        return wagonTypes;
    }
}
