package jfreerails.world.track;

import jfreerails.world.misc.FreerailsSerializable;

final public class TrackRuleProperties implements FreerailsSerializable {

	public String getTypeName() {
		return typeName;
	}

	public boolean isDoubleTrackEnabled() {
		return false;
	}
	public int getRuleNumber(){
		return number;
	}
	private final int rGBvalue;
	private final int number;		//This rule's position in the track rule list.

	private final boolean enableDoubleTrack;
	private final String typeName;
	
	
	public	TrackRuleProperties(int rgb, boolean doubleTrack, String name, int n){
		rGBvalue=rgb;
		enableDoubleTrack=doubleTrack;
		typeName=name;
		number=n;
		
	}
}