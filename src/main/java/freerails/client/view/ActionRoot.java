/*
 * Created on Apr 10, 2004
 */
package freerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.RenderersRoot;
import freerails.controller.StationBuilder;
import freerails.controller.TrackMoveProducer;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

/**
 * Provides access to Actions change the game state and the GUI.
 * 
 * @author Luke
 * 
 */
public class ActionRoot {

    private class BuildTrainDialogAction extends AbstractAction {
        private static final long serialVersionUID = 3257853173002416948L;

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

    private final BuildTrainDialogAction buildTrainDialogAction = new BuildTrainDialogAction();

    private DialogueBoxController dialogueBoxController = null;

    private final ServerControlModel serverControls;

    private StationBuildModel stationBuildModel;

    private TrackMoveProducer trackMoveProducer;

    public ActionRoot(ModelRootImpl mr) {
        this.serverControls = new ServerControlModel(mr);
    }

    public Action getBuildTrainDialogAction() {
        return buildTrainDialogAction;
    }

    public DialogueBoxController getDialogueBoxController() {
        return dialogueBoxController;
    }

    public ServerControlModel getServerControls() {
        return serverControls;
    }

    public StationBuildModel getStationBuildModel() {
        return stationBuildModel;
    }

    public TrackMoveProducer getTrackMoveProducer() {
        return trackMoveProducer;
    }

    public void setDialogueBoxController(
            DialogueBoxController dialogueBoxController) {
        this.dialogueBoxController = dialogueBoxController;
    }

    /**
     * Call this method when a new game is started or a game is loaded.
     */
    public void setup(ModelRootImpl modelRoot, RenderersRoot vl) {
        serverControls.setup(modelRoot, dialogueBoxController);
        if (!modelRoot.hasBeenSetup)
            throw new IllegalStateException();

        ReadOnlyWorld world = modelRoot.getWorld();

        if (world.size(SKEY.TRACK_RULES) > 0) {
            trackMoveProducer = new TrackMoveProducer(modelRoot);
            stationBuildModel = new StationBuildModel(new StationBuilder(
                    modelRoot), vl, modelRoot);
        }
    }

}