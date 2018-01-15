/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.view;

import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.RendererRoot;
import freerails.controller.StationBuilder;
import freerails.controller.TrackMoveProducer;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Provides access to Actions change the game state and the GUI.
 */
public class ActionRoot {

    private final Action buildTrainDialogAction = new BuildTrainDialogAction();
    private final ServerControlModel serverControls;
    private DialogueBoxController dialogueBoxController = null;
    private StationBuildModel stationBuildModel;
    private TrackMoveProducer trackMoveProducer;

    /**
     * @param mr
     */
    public ActionRoot(ModelRootImpl mr) {
        serverControls = new ServerControlModel(mr);
    }

    /**
     * @return
     */
    public Action getBuildTrainDialogAction() {
        return buildTrainDialogAction;
    }

    /**
     * @return
     */
    public DialogueBoxController getDialogueBoxController() {
        return dialogueBoxController;
    }

    /**
     * @param dialogueBoxController
     */
    public void setDialogueBoxController(DialogueBoxController dialogueBoxController) {
        this.dialogueBoxController = dialogueBoxController;
    }

    /**
     * @return
     */
    public ServerControlModel getServerControls() {
        return serverControls;
    }

    /**
     * @return
     */
    public StationBuildModel getStationBuildModel() {
        return stationBuildModel;
    }

    /**
     * @return
     */
    public TrackMoveProducer getTrackMoveProducer() {
        return trackMoveProducer;
    }

    /**
     * Call this method when a new game is started or a game is loaded.
     */
    public void setup(ModelRootImpl modelRoot, RendererRoot vl) {
        serverControls.setup(modelRoot, dialogueBoxController);
        if (!modelRoot.hasBeenSetup) throw new IllegalStateException();

        ReadOnlyWorld world = modelRoot.getWorld();

        if (world.size(SKEY.TRACK_RULES) > 0) {
            trackMoveProducer = new TrackMoveProducer(modelRoot);
            stationBuildModel = new StationBuildModel(new StationBuilder(modelRoot), vl, modelRoot);
        }
    }

    private class BuildTrainDialogAction extends AbstractAction {
        private static final long serialVersionUID = 3257853173002416948L;

        private BuildTrainDialogAction() {
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