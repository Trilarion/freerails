package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import jfreerails.client.common.ActionAdapter;
import jfreerails.client.renderer.TrackPieceRenderer;
import jfreerails.client.renderer.TrackPieceRendererList;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;

/**
 * provides the models for the TrackMoveProducer build mode
 */
public class TrackBuildModel {
    /*
     * 100 010 001 = 0x111
     */
    private static final int trackTemplate =
        TrackConfiguration.getFlatInstance(0x111).getTemplate();
 
    private ActionAdapter buildModeAdapter;

    private ActionAdapter trackRuleAdapter;

    private TrackMoveProducer trackMoveProducer;

    private ViewLists viewLists;

    private ReadOnlyWorld world;

    public ActionAdapter getBuildModeActionAdapter() {
	return buildModeAdapter;
    }

    public ActionAdapter getTrackRuleAdapter() {
	return trackRuleAdapter;
    }

    private class BuildModeAction extends AbstractAction {
	private int actionId;

	private BuildModeAction(int actionId, String name) {
	    putValue(NAME, name);
	    putValue(ACTION_COMMAND_KEY, name);
	    this.actionId = actionId;
	}
	    
	public void actionPerformed(ActionEvent e) {
	    if (! (e.getSource() instanceof ActionAdapter))
		return;
		    
	    trackMoveProducer.setTrackBuilderMode(actionId);
	}
    }

    private class TrackRuleAction extends AbstractAction {
	private int actionId;

	private TrackRuleAction(int actionId, String name) {
            TrackPieceRendererList trackPieceRendererList =
                viewLists.getTrackPieceViewList();
	    putValue(NAME, name);
	    putValue(ACTION_COMMAND_KEY, name);
	    this.actionId = actionId;
            TrackRule trackRule = (TrackRule) world.get(KEY.TRACK_RULES,
		    actionId);
            int ruleNumber = trackRule.getRuleNumber();
            TrackPieceRenderer renderer =
		trackPieceRendererList.getTrackPieceView(ruleNumber);
	    putValue(SMALL_ICON, new
		    ImageIcon(renderer.getTrackPieceIcon(trackTemplate)));
            putValue(SHORT_DESCRIPTION, trackRule.getTypeName());
	}
	    
	public void actionPerformed(ActionEvent e) {
	    if (! (e.getSource() instanceof ActionAdapter))
		return;
		    
	    trackMoveProducer.setTrackRule(actionId);
	}
    }

    public TrackBuildModel(TrackMoveProducer tmp, ReadOnlyWorld world, ViewLists
	vl) {
	this.world = world;
	viewLists = vl;
	trackMoveProducer = tmp;
	/* set up build modes */
	BuildModeAction[] actions = new BuildModeAction[] {
	    new BuildModeAction(TrackMoveProducer.BUILD_TRACK, "Build Track"),
	    new BuildModeAction(TrackMoveProducer.REMOVE_TRACK, "Remove Track"),
	    new BuildModeAction(TrackMoveProducer.UPGRADE_TRACK,
	    "Upgrade Track"),
	    new BuildModeAction(TrackMoveProducer.IGNORE_TRACK, "View Mode")};
	buildModeAdapter = new ActionAdapter(actions);    
	/* set up track actions */
	Vector actionsVector = new Vector();
	for (int i = 0; i < world.size(KEY.TRACK_RULES); i++) {
	    TrackRule trackRule = (TrackRule)world.get(KEY.TRACK_RULES, i);
	    if (!trackRule.isStation()) { 
		actionsVector.add(new TrackRuleAction(i, trackRule.getTypeName()));
	    }
	}
	trackRuleAdapter = new ActionAdapter((Action[]) actionsVector.toArray(new Action[0]));
    }
}
