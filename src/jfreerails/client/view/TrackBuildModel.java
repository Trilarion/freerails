package jfreerails.client.view;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import jfreerails.client.common.ActionAdapter;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.TrackPieceRenderer;
import jfreerails.client.renderer.TrackPieceRendererList;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/**
 * Provides the models for the TrackMoveProducer build mode.
 * @author rob
 */
public class TrackBuildModel {
    /*
     * 100 010 001 = 0x111
     */
    private static final int trackTemplate = TrackConfiguration.getFlatInstance(0x111)
                                                               .get9bitTemplate();
    private final ActionAdapter buildModeAdapter;
    private final ActionAdapter trackRuleAdapter;
    private final TrackMoveProducer trackMoveProducer;
    private final ViewLists viewLists;
    private final ReadOnlyWorld world;
    private final StationBuildModel stationBuildModel;
    private final ModelRoot modelRoot;
	private final HashMap<Integer, Action> buildModeActionsHM = new HashMap<Integer, Action>(); 
  
    public ActionAdapter getBuildModeActionAdapter() {
        return buildModeAdapter;
    }

    public ActionAdapter getTrackRuleAdapter() {
        return trackRuleAdapter;
    }

    private class BuildModeAction extends AbstractAction {
        private final int actionId;

        private BuildModeAction(int actionId, String name) {
            putValue(NAME, name);
            putValue(ACTION_COMMAND_KEY, name);
            this.actionId = actionId;
        }

        public void actionPerformed(ActionEvent e) {
        	 cancelStationPlacement();
            if (!(e.getSource() instanceof ActionAdapter))
                return;

            trackMoveProducer.setTrackBuilderMode(actionId);
        }
    }

    private class TrackRuleAction extends AbstractAction {
        private final int actionId;

        private TrackRuleAction(int actionId, String name) {
            TrackPieceRendererList trackPieceRendererList = viewLists.getTrackPieceViewList();
            putValue(NAME, name);
            putValue(ACTION_COMMAND_KEY, name);
            this.actionId = actionId;

            TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES,
                    actionId);
            int ruleNumber = actionId;
            TrackPieceRenderer renderer = trackPieceRendererList.getTrackPieceView(ruleNumber);

            /* create a scaled image */
            Image unscaledImage = renderer.getTrackPieceIcon(trackTemplate);
            Image scaledImage = unscaledImage.getScaledInstance(unscaledImage.getWidth(
                        null) * 3 / 4, unscaledImage.getHeight(null) * 3 / 4,
                    Image.SCALE_SMOOTH);

            putValue(SMALL_ICON, new ImageIcon(scaledImage));
            putValue(SHORT_DESCRIPTION,
                trackRule.getTypeName() + " \n $" + trackRule.getPrice());
        }

        public void actionPerformed(ActionEvent e) {
        	 //Cancel build station mode..
        	 cancelStationPlacement();
            
            //Not sure why the following is here, LL
            if (!(e.getSource() instanceof ActionAdapter))
                return;

            trackMoveProducer.setTrackRule(actionId);
            System.err.println("SELECTED_TRACK_TYPE="+actionId);
            //modelRoot.setProperty(ModelRoot.Property.SELECTED_TRACK_TYPE, new Integer(actionId));
           
        }
    }

    public Action getBuildModeAction(int id){
    	return buildModeActionsHM.get(new Integer(id));
    }
    
    public TrackBuildModel(TrackMoveProducer tmp, ModelRoot modelRoot,
        ViewLists vl, StationBuildModel stationBuildModel) {
    	this.modelRoot = modelRoot;
        this.world = modelRoot.getWorld();
        viewLists = vl;
        trackMoveProducer = tmp;
        this.stationBuildModel = stationBuildModel;

        BuildModeAction[] buildModeActions = new BuildModeAction[] {
		                new BuildModeAction(TrackMoveProducer.BUILD_TRACK, "Build Track"),
		                new BuildModeAction(TrackMoveProducer.REMOVE_TRACK,
		                    "Remove Track"),
		                new BuildModeAction(TrackMoveProducer.UPGRADE_TRACK,
		                    "Upgrade Track"),
		                new BuildModeAction(TrackMoveProducer.IGNORE_TRACK, "View Mode")
		            };
		buildModeAdapter = new ActionAdapter(buildModeActions);

		buildModeActionsHM.put(new Integer(TrackMoveProducer.BUILD_TRACK), buildModeActions[0]);
		buildModeActionsHM.put(new Integer(TrackMoveProducer.REMOVE_TRACK), buildModeActions[1]);
		buildModeActionsHM.put(new Integer(TrackMoveProducer.UPGRADE_TRACK), buildModeActions[2]);
		buildModeActionsHM.put(new Integer(TrackMoveProducer.IGNORE_TRACK), buildModeActions[3]);
		
        /* set up track actions */
        Vector actionsVector = new Vector();

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, i);

            if (!trackRule.isStation()) {
                actionsVector.add(new TrackRuleAction(i, trackRule.getTypeName()));
            }
        }

        trackRuleAdapter = new ActionAdapter((Action[])actionsVector.toArray(
                    new Action[0]));
    }

	private void cancelStationPlacement() {
		//Cancel build station mode..
		stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(
		        this,
		        ActionEvent.ACTION_PERFORMED, ""));
	}
}