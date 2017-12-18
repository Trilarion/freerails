/*
 * Created on 25-Aug-2003
 */
package freerails.move;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.train.ImmutableSchedule;

/**
 * This Move changes a train's schedule.
 *
 * @author Luke Lindsay
 */
public class ChangeTrainScheduleMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3691043187930052149L;

    /**
     *
     * @param id
     * @param before
     * @param after
     * @param p
     */
    public ChangeTrainScheduleMove(int id, ImmutableSchedule before,
                                   ImmutableSchedule after, FreerailsPrincipal p) {
        super(KEY.TRAIN_SCHEDULES, id, before, after, p);
    }
}