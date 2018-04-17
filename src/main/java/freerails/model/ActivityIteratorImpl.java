package freerails.model;

import freerails.model.player.FreerailsPrincipal;
import freerails.model.world.FullWorld;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 */
public class ActivityIteratorImpl implements ActivityIterator {

    /**
     *
     */
    private final int size;
    private final List<FullWorld.ActivityAndTime> currentList;

    /**
     *
     */
    private int activityIndex = 0;
    private FullWorld.ActivityAndTime activityAndTime;

    /**
     * @param principal
     * @param index
     */
    public ActivityIteratorImpl(FullWorld world, FreerailsPrincipal principal, int index) {
        currentList = world.activities.get(principal).get(index);
        size = currentList.size();
        activityAndTime = currentList.get(activityIndex);
    }

    public double absoluteToRelativeTime(double absoluteTime) {
        double dt = absoluteTime - activityAndTime.startTime;
        dt = Math.min(dt, activityAndTime.act.duration());
        return dt;
    }

    /**
     * @return
     */
    public Activity getActivity() {
        return activityAndTime.act;
    }

    /**
     * @return
     */
    public double getDuration() {
        return activityAndTime.act.duration();
    }

    public double getFinishTime() {
        return activityAndTime.startTime + activityAndTime.act.duration();
    }

    public double getStartTime() {
        return activityAndTime.startTime;
    }

    /**
     * @param absoluteTime
     * @return
     */
    public Serializable getState(double absoluteTime) {
        double dt = absoluteToRelativeTime(absoluteTime);
        return activityAndTime.act.getStateAtTime(dt);
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return (activityIndex + 1) < size;
    }

    /**
     *
     */
    public void nextActivity() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        activityIndex++;
        activityAndTime = currentList.get(activityIndex);
    }

    /**
     *
     */
    public void gotoLastActivity() {
        activityIndex = size - 1;
        activityAndTime = currentList.get(activityIndex);
    }

    /**
     * @return
     */
    public boolean hasPrevious() {
        return activityIndex >= 1;
    }

    /**
     * @throws NoSuchElementException
     */
    public void previousActivity() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        activityIndex--;
        activityAndTime = currentList.get(activityIndex);
    }

}
