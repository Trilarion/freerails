package freerails.move;

import freerails.util.Point2D;

import java.io.Serializable;

/**
 *
 */
class MapDiff implements Serializable {

    private static final long serialVersionUID = -5935670372745313360L;
    private final Serializable before;
    private final Serializable after;
    private final Point2D p;

    MapDiff(Serializable before, Serializable after, Point2D p) {
        this.after = after;
        this.before = before;
        this.p = new Point2D(p);
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

    public Point2D getP() {
        return p;
    }
}
