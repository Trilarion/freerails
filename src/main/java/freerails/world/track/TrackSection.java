package freerails.world.track;

import freerails.world.FreerailsSerializable;
import freerails.world.common.ImPoint;
import freerails.world.common.Step;

/**
 * Represents the track connecting two adjacent tiles.
 *
 */
public class TrackSection implements FreerailsSerializable {

    private static final long serialVersionUID = -3776624056097990938L;
    private final Step step;
    private final ImPoint tile;

    /**
     *
     * @param step
     * @param tile
     */
    public TrackSection(final Step step, final ImPoint tile) {
        ImPoint otherTile = Step.move(tile, step);
        if (tile.compareTo(otherTile) > 0) {
            this.step = step.getOpposite();
            this.tile = otherTile;
        } else {
            this.step = step;
            this.tile = tile;
        }
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((step == null) ? 0 : step.hashCode());
        result = PRIME * result + ((tile == null) ? 0 : tile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TrackSection other = (TrackSection) obj;
        if (step == null) {
            if (other.step != null)
                return false;
        } else if (!step.equals(other.step))
            return false;
        if (tile == null) {
            return other.tile == null;
        } else return tile.equals(other.tile);
    }

    @Override
    public String toString() {
        return tile.toString() + " " + step.toString();
    }

    /**
     *
     * @return
     */
    public ImPoint tileA() {
        return tile;
    }

    /**
     *
     * @return
     */
    public ImPoint tileB() {
        return Step.move(tile, step);
    }

}
