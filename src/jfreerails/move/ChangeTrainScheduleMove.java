
package jfreerails.move;

import jfreerails.world.train.Schedule;


/**
 *
 *
 *
 * @author lindsal
 */

final public class ChangeTrainScheduleMove {

    private final Schedule oldSchedule;

    private final Schedule newSchedule;



    public Schedule getOldSchedule() {
        return oldSchedule;
    }

    public Schedule getNewSchedule() {
        return newSchedule;
    }

    public ChangeTrainScheduleMove(Schedule before, Schedule after){
    	oldSchedule=before;
    	newSchedule=after;
    }
}





