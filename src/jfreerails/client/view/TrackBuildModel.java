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
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.IGNORE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.REMOVE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.UPGRADE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_STATION;
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
    private static final int trackTemplate = TrackConfiguration.from9bitTemplate(0x111)
                                                               .get9bitTemplate();
    private final ActionAdapter buildModeAdapter;
    private final ActionAdapter trackRuleAdapter;
    private final TrackMoveProducer trackMoveProducer;
    private final ViewLists viewLists;
    private final ReadOnlyWorld world;
    private final StationBuildModel stationBuildModel;
    private final HashMap<TrackMoveProducer.BuildMode, Action> buildModeActionsHM = new HashMap<TrackMoveProducer.BuildMode, Action>(); 
  
    public ActionAdapter getBuildModeActionAdapter() {
        return buildModeAdapter;
    }

    public ActionAdapter getTrackRuleAdapter() {
        return trackRuleAdapter;
    }

    private class BuildModeAction extends AbstractAction {
        private final TrackMoveProducer.BuildMode mode;

        private BuildModeAction(TrackMoveProducer.BuildMode mode, String name) {
            putValue(NAME, name);
            putValue(ACTION_COMMAND_KEY, name);
            this.mode = mode;
        }

        public void actionPerformed(ActionEvent e) {
        	 cancelStationPlacement();
            if (!(e.getSource() instanceof ActionAdapter))
                return;

            trackMoveProducer.setTrackBuilderMode(mode);
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
           
            System.err.println("SELECTED_TRACK_TYPE="+actionId);
            //modelRoot.setProperty(ModelRoot.Property.SELECTED_TRACK_TYPE, new Integer(actionId));
           
        }
    }

    public Action getBuildModeAction(int id){
    	return buildModeActionsHM.get(new Integer(id));
    }
    
    public TrackBuildModel(TrackMoveProducer tmp, ModelRoot modelRoot,
        ViewLists vl, StationBuildModel stationBuildModel) {
    	this.world = modelRoot.getWorld();
        viewLists = vl;
        trackMoveProducer = tmp;
        this.stationBuildModel = stationBuildModel;

        BuildModeAction[] buildModeActions = new BuildModeAction[] {
		                new BuildModeAction(BUILD_TRACK, "Build Track"),
		                new BuildModeAction(REMOVE_TRACK,
		                    "Remove Track"),
		                new BuildModeAction(UPGRADE_TRACK,
		                    "Upgrade Track"),
		                new BuildModeAction(IGNORE_TRACK, "View Mode"),
		                new BuildModeAction(BUILD_STATION, "Build station")
		            };
		buildModeAdapter = new ActionAdapter(buildModeActions);

		buildModeActionsHM.put(BUILD_TRACK, buildModeActions[0]);
		buildModeActionsHM.put(REMOVE_TRACK, buildModeActions[1]);
		buildModeActionsHM.put(UPGRADE_TRACK, buildModeActions[2]);
		buildModeActionsHM.put(IGNORE_TRACK, buildModeActions[3]);
		
        /* set up track actions */
        Vector<Action> actionsVector = new Vector<Action>();

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, i);

            if (!trackRule.isStation()) {
                actionsVector.add(new TrackRuleAction(i, trackRule.getTypeName()));
            }
        }

        trackRuleAdapter = new ActionAdapter(actionsVector.toArray(
                    new Action[0]));
    }

	private void cancelStationPlacement() {
		//Cancel build station mode..
		stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(
		        this,
		        ActionEvent.ACTION_PERFORMED, ""));
	}
}