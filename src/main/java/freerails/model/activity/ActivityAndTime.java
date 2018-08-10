package freerails.model.activity;

import java.io.Serializable;

// TODO include time in Activity
/**
 *
 */
public class ActivityAndTime implements Serializable {

    private static final long serialVersionUID = -5149207279086814649L;

    /**
     *
     */
    public final Activity act;

    /**
     *
     */
    public final double startTime;

    public ActivityAndTime(Activity act, double time) {
        this.act = act;
        startTime = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ActivityAndTime)) return false;

        final ActivityAndTime activityAndTime = (ActivityAndTime) obj;

        if (!act.equals(activityAndTime.act)) return false;
        return !(startTime != activityAndTime.startTime);
    }

    @Override
    public int hashCode() {
        int result;
        result = act.hashCode();
        result = 29 * result + (int) startTime;
        return result;
    }
}
