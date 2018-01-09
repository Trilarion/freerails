package freerails.world;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 */
public class ActivityIteratorImpl implements ActivityIterator {

    private WorldImpl world;
    /**
     *
     */
    public final int size;
    private final List<WorldImpl.ActivityAndTime> currentList;

    /**
     *
     */
    public int activityIndex = 0;
    private WorldImpl.ActivityAndTime ant;

    /**
     * @param playerIndex
     * @param index
     */
    public ActivityIteratorImpl(WorldImpl world, int playerIndex, int index) {
        this.world = world;
        currentList = world.activityLists.get(playerIndex, index);
        size = currentList.size();
        ant = currentList.get(activityIndex);
    }

    public double absoluteToRelativeTime(double t) {
        double dt = t - ant.startTime;
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
     * @param t
     * @return
     */
    public Serializable getState(double t) {
        double dt = absoluteToRelativeTime(t);
        return ant.act.getState(dt);
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
