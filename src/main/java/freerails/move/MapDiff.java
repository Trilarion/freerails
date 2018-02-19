package freerails.move;

import freerails.util.Vector2D;

import java.io.Serializable;

/**
 *
 */
public class MapDiff implements Serializable {

    private static final long serialVersionUID = -5935670372745313360L;
    private final Serializable before;
    private final Serializable after;
    private final Vector2D location;

    public MapDiff(Serializable before, Serializable after, Vector2D location) {
        this.after = after;
        this.before = before;
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MapDiff)) return false;

        final MapDiff diff = (MapDiff) obj;

        if (location.x != diff.location.x) return false;
        if (location.y != diff.location.y) return false;
        if (!after.equals(diff.after)) return false;
        return before.equals(diff.before);
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + before.hashCode();
        result = 29 * result + after.hashCode();
        return result;
    }

    public Serializable getBefore() {
        return before;
    }

    public Serializable getAfter() {
        return after;
    }

    public Vector2D getLocation() {
        return location;
    }
}
