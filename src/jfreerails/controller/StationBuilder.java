package jfreerails.controller;

import java.awt.Point;

import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;
import jfreerails.world.track.TrackRuleList;
import jfreerails.world.station.StationList;

/**
 * @author Luke Lindsay 08-Nov-2002
 *
 */
public class StationBuilder {
	
	TrackMoveProducer trackMoveProducer;
	
	TrackRuleList trackRuleList;
	StationList stationList;

	int ruleNumber;

	/*public StationBuilder(TrackMoveProducer tmp, TrackRuleList rules){
		this.trackMoveProducer=tmp;
		this.trackRuleList=rules;
		TrackRule trackRule;
		int i=-1;
		do{
			i++;
			trackRule=trackRuleList.getTrackRule(i);						
		}while(!trackRule.isStation());
		ruleNumber=i;
	}*/

	public StationBuilder(TrackMoveProducer tmp, World w) {
		this.trackMoveProducer = tmp;
		
		this.trackRuleList = w.getTrackRuleList();
		TrackRule trackRule;
		int i=-1;
		do {
			i++;
			trackRule=trackRuleList.getTrackRule(i);						
		} while(!trackRule.isStation());
		ruleNumber=i;
		
		stationList = w.getStationList();

	}
	
	
	public void buildStation(Point p){
		int oldMode = trackMoveProducer.getTrackBuilderMode();
		trackMoveProducer.setTrackBuilderMode(TrackMoveProducer.UPGRADE_TRACK);		
		int oldTrackRule=trackMoveProducer.getTrackRule();
		trackMoveProducer.setTrackRule(this.ruleNumber);
		trackMoveProducer.upgradeTrack(p);
		trackMoveProducer.setTrackRule(oldTrackRule);
		trackMoveProducer.setTrackBuilderMode(oldMode);		
		stationList.addStation(p);

		//added by Scott Bennett for testing 14/03/03
		Point q;
		System.out.println("stationList.size() is " + stationList.size());
		for (int i=0; i<stationList.size(); i++) {
			q = stationList.getStation(i);
			System.out.println("station #" + i + " is at (" + q.getX() + "," + q.getY() + ")");
		}
		//added by Scott Bennett for testing 12/03/03
		System.out.println("StationBuilder: Station built at (" + p.getX() + "," + p.getY() + ")");
		
	}
	
	public TrackRuleList getTrackRuleList(){
		return trackRuleList;
	}
	
	public void setStationType(int ruleNumber){
		this.ruleNumber=ruleNumber;			
	}
	
	
	
}
