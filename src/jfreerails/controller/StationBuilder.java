package jfreerails.controller;

import java.awt.Point;

import jfreerails.world.track.TrackRule;
import jfreerails.world.track.TrackRuleList;

/**
 * @author Luke Lindsay 08-Nov-2002
 *
 */
public class StationBuilder {
	
	TrackMoveProducer trackMoveProducer;
	
	TrackRuleList trackRuleList;

	int ruleNumber;

	public StationBuilder(TrackMoveProducer tmp, TrackRuleList rules){
		this.trackMoveProducer=tmp;
		this.trackRuleList=rules;
		TrackRule trackRule;
		int i=-1;
		do{
			i++;
			trackRule=trackRuleList.getTrackRule(i);						
		}while(!trackRule.isStation());
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
	}
	
	public TrackRuleList getTrackRuleList(){
		return trackRuleList;
	}
	
	public void setStationType(int ruleNumber){
		this.ruleNumber=ruleNumber;			
	}
	
	
	
}
