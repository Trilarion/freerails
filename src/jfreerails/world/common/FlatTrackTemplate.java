
package jfreerails.world.common;

public interface FlatTrackTemplate extends FreerailsSerializable {
    FlatTrackTemplate getRotatedInstance(Rotation r);
    boolean contains(FlatTrackTemplate ftt);
    int getTemplate();
	int getNewTemplateNumber();
}
