/**
 * @author Luke Lindsay 08-Nov-2002
 *  
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 * 
 * Class to build a station at a given point, names station after nearest
 * city. If that name is taken then a "Junction" or "Siding" is added to 
 * the name. 
 */

package jfreerails.controller;

import java.awt.Point;

import jfreerails.world.station.StationModel;
import jfreerails.world.station.naming.CalcNearestCity;
import jfreerails.world.station.naming.VerifyStationName;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;

public class StationBuilder {
	
	private TrackMoveProducer trackMoveProducer;
	private World w;
	private int ruleNumber;

	public StationBuilder(TrackMoveProducer tmp, World world) {
		this.trackMoveProducer = tmp;
		this.w = world;		
		TrackRule trackRule;
		
		int i = -1;
		do {
			i++;
			trackRule=(TrackRule)w.get(KEY.TRACK_RULES, i);						
		} while(!trackRule.isStation());
		
		ruleNumber=i;		
	}
	
	public void buildStation(Point p){
		String cityName;
		String stationName;
		int oldMode = trackMoveProducer.getTrackBuilderMode();
		int oldTrackRule = trackMoveProducer.getTrackRule();
		
		trackMoveProducer.setTrackBuilderMode(TrackMoveProducer.UPGRADE_TRACK);		
		trackMoveProducer.setTrackRule(this.ruleNumber);
		trackMoveProducer.upgradeTrack(p);
		trackMoveProducer.setTrackBuilderMode(oldMode);
		trackMoveProducer.setTrackRule(oldTrackRule);
			
		CalcNearestCity cNC = new CalcNearestCity(w, p.x, p.y);
		cityName = cNC.findNearestCity();
		
		VerifyStationName vSN = new VerifyStationName(w, cityName);
		stationName = vSN.getName();
		
		if (stationName == null) {
			//there are no cities, this should never happen
			stationName = "Central Station";
		}
				
		//check the terrain to see if we can build a station on it...
		
		w.add(KEY.STATIONS, new StationModel(p.x, p.y, stationName));	
		
		System.out.println(stationName + " built at (" + (int)p.getX() + "," + (int)p.getY() + ")");		
	}
	
	public World getWorld(){
		return w;
	}
	
	public void setStationType(int ruleNumber){
		this.ruleNumber=ruleNumber;			
	}
		
}
