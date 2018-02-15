package freerails.model;

import java.io.Serializable;

/**
 *
 */
public class TestActivity implements Activity {

    private static final long serialVersionUID = 1298936498785131183L;

    private final double duration;

    /**
     * @param duration
     */
    public TestActivity(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TestActivity))
            return false;

        final TestActivity testActivity = (TestActivity) obj;

        return !(duration != testActivity.duration);
    }

    @Override
    public int hashCode() {
        return (int) duration;
    }

    /**
     * @return
     */
    public double duration() {
        return duration;
    }

    /**
     * @param time
     * @return
     */
    public Serializable getStateAtTime(double time) {
        return new TestState((int) time);
    }

    @Override
    public String toString() {
        return getClass().getName() + '{' + duration + '}';
    }
}
