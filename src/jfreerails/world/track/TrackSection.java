package jfreerails.world.track;



import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Step;

/**
 * Represents the track connecting two adjacent tiles.
 * 
 * @author Luke
 *
 */
public class TrackSection implements FreerailsSerializable {
		
	private static final long serialVersionUID = -3776624056097990938L;
	private final Step step;
	private final ImPoint tile;
	public TrackSection(final Step step, final ImPoint tile) {
		ImPoint otherTile = Step.move(tile, step);
		if(tile.compareTo(otherTile) > 0){
			this.step = step.getOpposite();
			this.tile = otherTile;
		}else{
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
			if (other.tile != null)
				return false;
		} else if (!tile.equals(other.tile))
			return false;
		return true;
	}
	@Override
	public String toString() {		
		return tile.toString()+ " "+ step.toString();
	}

	public ImPoint tileA(){
		return tile;
	}
	public ImPoint tileB(){
		return Step.move(tile, step);
	}
	
}
