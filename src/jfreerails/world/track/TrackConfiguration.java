package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;

import jfreerails.world.common.FlatTrackTemplate;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.Rotation;

/**
 * An instance of this class represents one of the possible track configurations
 * in a map square - the cobinations of directions in which track can be
 * laid. Instances of this class cannot be created and must be obtained via the
 * static methods herein.
 */
final public class TrackConfiguration implements FlatTrackTemplate {
	
	private static final ArrayList flatTrackConfigurations = new ArrayList(512);

	static {
		for (int i = 0; i < 512; i++) {
			flatTrackConfigurations.add(i, new TrackConfiguration(i));
		}
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

	/**
	 * Not implemented.
	 */
	public static TrackConfiguration getFlatInstance(OneTileMoveVector v) {
		return null;
	}

	/**
	 * @return the superposition of two track templates
	 */
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

	/**
	 * @return the TrackConfiguration representing the track section c minus
	 * the track sections represented by v.
	 */
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

	/**
	 * @return an int representing this track configuration.
	 */
	public int getTemplate() {
		return configuration;
	}
	public boolean equals(Object o) {
		return o == this;		
	}

	public int hashCode() {
		return configuration;
	}

	public int getNewTemplateNumber() {
		int newTemplate = 0;
		OneTileMoveVector[] vectors = OneTileMoveVector.getList();
		for (int i = 0; i < vectors.length ; i ++){
			if(this.contains(vectors[i])){
				newTemplate = newTemplate | vectors[i].getNewTemplateNumber();
			}
		}
		return newTemplate;
	}

}
