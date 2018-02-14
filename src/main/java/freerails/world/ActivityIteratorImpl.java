package freerails.world;

import freerails.world.world.FullWorld;

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
    private FullWorld.ActivityAndTime ant;

    /**
     * @param playerIndex
     * @param index
     */
    public ActivityIteratorImpl(FullWorld world, int playerIndex, int index) {
        currentList = world.activityLists.get(playerIndex, index);
        size = currentList.size();
        ant = currentList.get(activityIndex);
    }

    public double absoluteToRelativeTime(double absoluteTime) {
        double dt = absoluteTime - ant.startTime;
        dt = Math.min(dt, ant.act.duration());
        return dt;
    }

    /**
     * @return
     */
    public Activity getActivity() {
        return ant.act;
    }

    /**
     * @return
     */
    public double getDuration() {
        return ant.act.duration();
    }

    public double getFinishTime() {
        return ant.startTime + ant.act.duration();
    }

    public double getStartTime() {
        return ant.startTime;
    }

    /**
     * @param absoluteTime
     * @return
     */
    public Serializable getState(double absoluteTime) {
        double dt = absoluteToRelativeTime(absoluteTime);
        return ant.act.getStateAtTime(dt);
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
        ant = currentList.get(activityIndex);
    }

    /**
     *
     */
    public void gotoLastActivity() {
        activityIndex = size - 1;
        ant = currentList.get(activityIndex);
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
        ant = currentList.get(activityIndex);
    }

}
