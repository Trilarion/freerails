package freerails.model.activity;

import freerails.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

// TODO what about a standard list iterator?
/**
 *
 */
public class ActivityIterator {

    /**
     *
     */
    private final List<Pair<Activity, Double>> activities;

    /**
     *
     */
    private int currentIndex;
    private Pair<Activity, Double> currentActivityWithTime;

    /**
     */
    public ActivityIterator(@NotNull List<Pair<Activity, Double>> activities) {
        this.activities = activities;
        currentIndex = 0;
        currentActivityWithTime = this.activities.get(currentIndex);
    }

    /**
     * @return
     */
    public Activity getActivity() {
        return currentActivityWithTime.getA();
    }

    public double getStartTime() {
        return currentActivityWithTime.getB();
    }

    /**
     * @return
     */
    public boolean hasNext() {
        return (currentIndex + 1) < activities.size();
    }

    /**
     *
     */
    public void nextActivity() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentIndex++;
        currentActivityWithTime = activities.get(currentIndex);
    }

    /**
     *
     */
    public void gotoLastActivity() {
        currentIndex = activities.size() - 1;
        currentActivityWithTime = activities.get(currentIndex);
    }

    /**
     * @return
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * @throws NoSuchElementException
     */
    public void previousActivity() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentIndex--;
        currentActivityWithTime = activities.get(currentIndex);
    }

}
