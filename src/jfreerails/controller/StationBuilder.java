package jfreerails.controller;

import java.awt.Point;

import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;

/**
 * @author Luke Lindsay 08-Nov-2002
 *
 */
public class StationBuilder {
	
	TrackMoveProducer trackMoveProducer;
	
	World w;
	
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

	public StationBuilder(TrackMoveProducer tmp, World world) {
		this.trackMoveProducer = tmp;
		this.w=world;		
		TrackRule trackRule;
		int i=-1;
		do {
			i++;
			trackRule=(TrackRule)w.get(KEY.TRACK_RULES, i);						
		} while(!trackRule.isStation());
		ruleNumber=i;		
	}
	
	
	public void buildStation(Point p){
		int oldMode = trackMoveProducer.getTrackBuilderMode();
		trackMoveProducer.setTrackBuilderMode(TrackMoveProducer.UPGRADE_TRACK);		
		int oldTrackRule=trackMoveProducer.getTrackRule();
		trackMoveProducer.setTrackRule(this.ruleNumber);
		trackMoveProducer.upgradeTrack(p);
		trackMoveProducer.setTrackRule(oldTrackRule);
		trackMoveProducer.setTrackBuilderMode(oldMode);	
		w.add(KEY.STATIONS, new StationModel(p.x, p.y));	
		

		//added by Scott Bennett for testing 14/03/03
		StationModel q;
		System.out.println("stationList.size() is " + w.size(KEY.STATIONS));
		for (int i=0; i<w.size(KEY.STATIONS); i++) {
			q = (StationModel)w.get(KEY.STATIONS, i);
			System.out.println("station #" + i + " is at (" + q.x + "," + q.y + ")");
		}
		//added by Scott Bennett for testing 12/03/03
		System.out.println("StationBuilder: Station built at (" + p.getX() + "," + p.getY() + ")");
		
	}
	
	public World getWorld(){
		return w;
	}
	
	public void setStationType(int ruleNumber){
		this.ruleNumber=ruleNumber;			
	}
	
	
	
}
