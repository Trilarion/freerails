/*
 * Created on Apr 10, 2004
 */
package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.StationBuilder;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/**
 * Provides access to Actions change the game state and the GUI.
 *  @author Luke
 *
 */
public class ActionRoot {
    private TrackBuildModel trackBuildModel;
    private TrackMoveProducer trackMoveProducer;
    private StationBuildModel stationBuildModel;
    private DialogueBoxController dialogueBoxController = null;
    private final BuildTrainDialogAction buildTrainDialogAction = new BuildTrainDialogAction();
    private final ServerControlModel serverControls = new ServerControlModel( null);

    private class BuildTrainDialogAction extends AbstractAction {
        public BuildTrainDialogAction() {
            super("Build Train");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
            putValue(SHORT_DESCRIPTION, "Build a new train");
        }

        public void actionPerformed(ActionEvent e) {
            if (dialogueBoxController != null) {
                dialogueBoxController.showSelectEngine();
            }
        }
    }

    /**
     *  Call this method when a new game is started or a game is loaded.
     */
    public void setup(ModelRootImpl modelRoot, ViewLists vl) {
    	serverControls.setModelRoot(modelRoot);
        if (!modelRoot.hasBeenSetup)
            throw new IllegalStateException();

        ReadOnlyWorld world = modelRoot.getWorld();

        if (world.size(SKEY.TRACK_RULES) > 0) {
            trackMoveProducer = new TrackMoveProducer(modelRoot);           
            stationBuildModel = new StationBuildModel(new StationBuilder(
                        modelRoot), vl, modelRoot);
            trackBuildModel = new TrackBuildModel(trackMoveProducer, modelRoot, vl, stationBuildModel);
        }
    }

    public DialogueBoxController getDialogueBoxController() {
        return dialogueBoxController;
    }

    public TrackBuildModel getTrackBuildModel() {
        return trackBuildModel;
    }

    public StationBuildModel getStationBuildModel() {
        return stationBuildModel;
    }

    public Action getBuildTrainDialogAction() {
        return buildTrainDialogAction;
    }

    public TrackMoveProducer getTrackMoveProducer() {
        return trackMoveProducer;
    }

    public ActionRoot() {
    }

    public void setDialogueBoxController(
        DialogueBoxController dialogueBoxController) {
        this.dialogueBoxController = dialogueBoxController;
    }

    public ServerControlModel getServerControls() {
        return serverControls;
    }

    
}