package freerails.client.view;

import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.RenderersRoot;
import freerails.controller.StationBuilder;
import freerails.controller.TrackMoveProducer;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Provides access to Actions change the game state and the GUI.
 *
 */
public class ActionRoot {

    private final BuildTrainDialogAction buildTrainDialogAction = new BuildTrainDialogAction();
    private final ServerControlModel serverControls;
    private DialogueBoxController dialogueBoxController = null;
    private StationBuildModel stationBuildModel;
    private TrackMoveProducer trackMoveProducer;

    /**
     *
     * @param mr
     */
    public ActionRoot(ModelRootImpl mr) {
        this.serverControls = new ServerControlModel(mr);
    }

    /**
     *
     * @return
     */
    public Action getBuildTrainDialogAction() {
        return buildTrainDialogAction;
    }

    /**
     *
     * @return
     */
    public DialogueBoxController getDialogueBoxController() {
        return dialogueBoxController;
    }

    /**
     *
     * @param dialogueBoxController
     */
    public void setDialogueBoxController(
            DialogueBoxController dialogueBoxController) {
        this.dialogueBoxController = dialogueBoxController;
    }

    /**
     *
     * @return
     */
    public ServerControlModel getServerControls() {
        return serverControls;
    }

    /**
     *
     * @return
     */
    public StationBuildModel getStationBuildModel() {
        return stationBuildModel;
    }

    /**
     *
     * @return
     */
    public TrackMoveProducer getTrackMoveProducer() {
        return trackMoveProducer;
    }

    /**
     * Call this method when a new game is started or a game is loaded.
     * @param modelRoot
     * @param vl
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

}