package jfreerails.world.track;

import jfreerails.world.misc.FlatTrackTemplate;
import jfreerails.world.misc.OneTileMoveVector;
import jfreerails.world.misc.Rotation;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;

final public class TrackConfiguration implements FlatTrackTemplate {

	/**
	 *  TrackConfiguration
	 */
	private static final ArrayList flatTrackConfigurations = new ArrayList(512);

	static {
		for (int i = 0; i < 512; i++) {
			flatTrackConfigurations.add(i, new TrackConfiguration(i));
		}
	}

	/** Returns a 9-bit value specifying  the track configuration, and
	 * hence the appropriate icon, for the track at this node.  E.g.
	 * the binary representation of a vertical straight would be:
	 * 010
	 * 010
	 * 010 i.e. 010010010
	 * @param railsList The rail list that is used ot generate the track
	 * graphic number.
	 * @return The track graphic number.
	 */
	public static int getTrackGraphicNumber(TrackSection[][] railsList) {
		int trackGraphicNumber = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (1 == x && 1 == y) {
					trackGraphicNumber = trackGraphicNumber | (1 << (3 * y + x));
				} else {
					if (null != railsList[x][y]) {
						trackGraphicNumber = trackGraphicNumber | (1 << (3 * y + x));
					}
				}
			}
		}
		return trackGraphicNumber;
	}


	private final int configuration;
	//private TrackSectionVector[] vectors;

	private TrackConfiguration(int i) {
		configuration = i;
	}

	private Object readResolve() throws ObjectStreamException {
		return TrackConfiguration.getFlatInstance(this.configuration);
	}

	public int getTrackGraphicsNumber() {
		return configuration;
	}

	public Iterator getPossibleConfigurationsIterator() {
		return flatTrackConfigurations.iterator();
	}

	public static TrackConfiguration getFlatInstance(int i) {
		return (TrackConfiguration) (flatTrackConfigurations.get(i));
	}

	public static TrackConfiguration getFlatInstance(String template) {
		int i = LegalTrackConfigurations.stringTemplate2Int(template);
		return (TrackConfiguration) (flatTrackConfigurations.get(i));
	}

	public static TrackConfiguration getFlatInstance(OneTileMoveVector v) {
		return null;
	}

	public static TrackConfiguration add(FlatTrackTemplate c, FlatTrackTemplate v) {
		/*
		int x=v.getX()+1;
		int y=v.getY()+1;
		int oldTemplate =c.getTrackGraphicsNumber();
		int newTemplate = oldTemplate | (1 << (3 * y + x));
		*/
		int newTemplate = c.getTemplate() | v.getTemplate();

		return getFlatInstance(newTemplate);
	}

	public static TrackConfiguration subtract(FlatTrackTemplate c, FlatTrackTemplate v) {
		/*
		int x=v.getX()+1;
		int y=v.getY()+1;
		int oldTemplate =c.getTrackGraphicsNumber();
		 int newTemplate = oldTemplate ^ (1 << (3 * y + x));
		 */
		int newTemplate = c.getTemplate() & (~v.getTemplate());
		return getFlatInstance(newTemplate);
	}

	public FlatTrackTemplate getRotatedInstance(Rotation r) {
		OneTileMoveVector[] list = OneTileMoveVector.getList();
		TrackConfiguration newTrackConfiguration = getFlatInstance("000010000");
		for (int i = 0; i < list.length; i++) {
			if (this.contains(list[i])) {
				newTrackConfiguration = add(newTrackConfiguration, list[i].getRotatedInstance(r));
			}
		}

		return newTrackConfiguration;
	}

	public boolean contains(FlatTrackTemplate ftt) {
		int template = ftt.getTemplate();
		return contains(template);
	}

	public boolean contains(int template) {
		if ((template | this.configuration) == this.configuration) {
			return true;
		} else {
			return false;
		}
	}

	public int getTemplate() {
		return configuration;
	}
}
