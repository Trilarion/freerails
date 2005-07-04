package jfreerails.world.common;

/**
 * Defines methods that encode a track configuration as an int.
 * 
 * @author Luke
 */
public interface FlatTrackTemplate extends FreerailsSerializable {
	/**
	 * @param ftt
	 *            the FlatTrackTemplate which may be a subset of this
	 *            FlatTrackTemplate.
	 * @return true if the vectors represented by this FlatTrackTemplate are a
	 *         superset of the vectors of the specified FlatTrackTemplate
	 */
	boolean contains(FlatTrackTemplate ftt);

	/**
	 * @return the integer representing the vector(s) of this object.
	 */
	int get9bitTemplate();
}