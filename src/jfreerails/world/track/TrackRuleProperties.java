package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;

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
	public boolean isStation(){
		return isStation;
	}
	
	private final int rGBvalue;
	private final int number;		//This rule's position in the track rule list.

	private final boolean enableDoubleTrack;
	private final String typeName;
	private final boolean isStation;
	private final int stationRadius;
	
	
	public	TrackRuleProperties(int rgb, boolean doubleTrack, String name, int n, boolean station, int radius){
		stationRadius=radius;
		rGBvalue=rgb;
		enableDoubleTrack=doubleTrack;
		typeName=name;
		number=n;
		isStation=station;		
	}
	
	public int getStationRadius() {
		return stationRadius;
	}

	public boolean equals(Object o) {
		if(o instanceof TrackRuleProperties){
			TrackRuleProperties test = (TrackRuleProperties)o;
			if(rGBvalue == test.getRGBvalue()
			&& number == test.getNumber()
			&& enableDoubleTrack == test.isEnableDoubleTrack()
			&& typeName.equals(test.getTypeName())
			&& isStation == test.isStation()
			&& stationRadius == test.stationRadius	){
				return true;
			}else{
				return false;
			}			
		}else{
			return false;
		}
	}

	public boolean isEnableDoubleTrack() {
		return enableDoubleTrack;
	}

	public int getNumber() {
		return number;
	}

	public int getRGBvalue() {
		return rGBvalue;
	}

}