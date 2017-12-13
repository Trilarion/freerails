/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import java.util.ArrayList;

import org.railz.client.common.UserMessageLogger;
import org.railz.client.renderer.ViewLists;
import org.railz.client.view.DialogueBoxController;
import org.railz.controller.MoveChainFork;
import org.railz.controller.ServerControlInterface;
import org.railz.controller.StationBuilder;
import org.railz.controller.TrackMoveProducer;
import org.railz.controller.UntriedMoveReceiver;
import org.railz.move.Move;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldListListener;

/**
 * Central point for accessing control models and common UI-independent services
 */
public final class ModelRoot {
    private TrackBuildModel trackBuildModel;
    private TrackMoveProducer trackMoveProducer;
    private StationBuildModel stationBuildModel;
    private DebugModel debugModel = new DebugModel();
    private  UntriedMoveReceiver moveReceiver;
    private  MoveChainFork moveFork;
    private MapCursor cursor = null;
    private DialogueBoxController dialogueBoxController = null;
    private BuildTrainDialogAction buildTrainDialogAction = new
	BuildTrainDialogAction();
    private FreerailsPrincipal playerPrincipal;
    private ViewLists viewLists;
    private UserMessageGenerator userMessageGenerator = null;
 
    private ArrayList listeners = new ArrayList();

    protected ServerControlModel serverControls = new ServerControlModel(null,
	    this);
    private ReadOnlyWorld world;
      
    public void addModelRootListener(ModelRootListener l) {
	synchronized(listeners) {
	    listeners.add(l);
	}
    }

    /**
     * @return the principal corresponding to the player this client is acting
     * for
     */
    public FreerailsPrincipal getPlayerPrincipal() {
	return playerPrincipal;
    }

    /**
     * set the principal corresponding to the player this client is acting for
     */
    public void setPlayerPrincipal(FreerailsPrincipal p) {
	assert p != null;
	playerPrincipal = p;
    }

    private class BuildTrainDialogAction extends AbstractAction {
	public BuildTrainDialogAction() {
	    super("Build Train");
	    putValue(ACCELERATOR_KEY,
		    KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
	    putValue(SHORT_DESCRIPTION, "Build a new train");
	}

	public void actionPerformed(ActionEvent e) {
	    if (dialogueBoxController != null) {
		dialogueBoxController.showSelectEngine();
	    }
	}
    }
 
    public MoveChainFork getMoveChainFork() {
        return moveFork;
    }

    public UntriedMoveReceiver getReceiver() {
        return moveReceiver;
    }

    public void setCursor(MapCursor c) {
        cursor = c;
    }

    public MapCursor getCursor() {
        return cursor;
    }

    /**
     * The default message logger logs to stderr
     */
    private UserMessageLogger messageLogger = new UserMessageLogger() {
            public void println(String s) {
                System.err.println(s);
            }
        };

    public void setWorld(ReadOnlyWorld w) {
	world = w;
    }

    /**
     * Updates the ModelRoot with those properties which are dependent upon the
     * world model.
     * Call this when the world model is changed (e.g. new map is loaded). At
     * this point setWorld(ReadOnlyWorld) should already have been called.
     */
    public void setWorld(UntriedMoveReceiver receiver,
    ViewLists vl) {
	viewLists = vl;
    	if(world.size(KEY.TRACK_RULES)> 0){
	    assert playerPrincipal != null;
	    trackMoveProducer = new TrackMoveProducer(world, receiver,
		    playerPrincipal);
	    trackBuildModel = new TrackBuildModel(trackMoveProducer, world, vl);
	    stationBuildModel = new StationBuildModel(new
		    StationBuilder(receiver, world, playerPrincipal), world,
		    vl);
    	}
	synchronized(listeners) {
	    for (int i = 0; i < listeners.size(); i++) {
		((ModelRootListener) listeners.get(i)).modelRootChanged();
	    }
	}

	if (userMessageGenerator != null)
	    moveFork.remove(userMessageGenerator);

        userMessageGenerator = new UserMessageGenerator(this, world);
        getMoveChainFork().add(userMessageGenerator);
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

    public UserMessageLogger getUserMessageLogger() {
        return messageLogger;
    }

    public void setUserMessageLogger(UserMessageLogger m) {
        messageLogger = m;
    }

    public void setDialogueBoxController(DialogueBoxController dialogueBoxController) {
		this.dialogueBoxController = dialogueBoxController;
	}


    public ViewLists getViewLists() {
	return viewLists;
    }
      
    /**
     * Not all clients may return a valid object - access to the server controls
     * is at the discretion of the server.
     */
    public ServerControlModel getServerControls() {
	return serverControls;
    }
  
    public void setServerControls(ServerControlInterface controls) {
	serverControls.setServerControlInterface(controls);
    }

    public ReadOnlyWorld getWorld() {
	return world;
    }

    public void setMoveFork(MoveChainFork moveFork) {
	this.moveFork = moveFork;
    }

    public void setMoveReceiver(UntriedMoveReceiver moveReceiver) {
	this.moveReceiver = moveReceiver;
    }

    public void addListListener(WorldListListener l) {
	moveFork.addListListener(l);
    }

    public void removeListListener(WorldListListener l) {
	moveFork.removeListListener(l);
     }

    public DebugModel getDebugModel() {
	return debugModel;
    }
}
