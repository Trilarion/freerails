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
    private final Vector2D p;

    public MapDiff(Serializable before, Serializable after, Vector2D p) {
        this.after = after;
        this.before = before;
        this.p = p;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MapDiff)) return false;

        final MapDiff diff = (MapDiff) obj;

        if (p.x != diff.p.x) return false;
        if (p.y != diff.p.y) return false;
        if (!after.equals(diff.after)) return false;
        return before.equals(diff.before);
    }

    @Override
    public int hashCode() {
        int result;
        result = p.hashCode();
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

    public Vector2D getP() {
        return p;
    }
}
