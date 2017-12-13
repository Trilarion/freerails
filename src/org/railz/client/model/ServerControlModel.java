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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import java.util.Enumeration;

import org.railz.client.common.*;
import org.railz.controller.ServerControlInterface;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * Exposes the ServerControlInterface to client UI implementations
 */
public class ServerControlModel {
    private ServerControlInterface serverInterface;
    private String currentDirectory = FileUtils.savesDirectory;
    private ModelRoot modelRoot;
    private ScreenHandler screenHandler;
    private Component dialog;

    /* TODO selection of scenario types, for now select first off the list */
    private class NewGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                String mapName = e.getActionCommand();

                if (mapName != null) {
                    serverInterface.newGame(mapName,
			    serverInterface.getScenarioNames()[0]);
                }
            }
        }

        public NewGameAction(String s) {
            if (s == null) {
                putValue(NAME, "New Game...");
            } else {
                putValue(NAME, s);
                putValue(ACTION_COMMAND_KEY, s);
            }
        }
    }

    private ActionAdapter selectMapActions;
    private Action newGameAction = new NewGameAction(null);

    private class SaveListener implements ActionListener {
	private JFileChooser chooser;

	public SaveListener(JFileChooser c) {
	    chooser = c;
	}

	public void actionPerformed(ActionEvent e) {
	    int option = JFileChooser.CANCEL_OPTION;
	    if (JFileChooser.APPROVE_SELECTION.equals
		    (e.getActionCommand())) {
		option = JFileChooser.APPROVE_OPTION;
	    }
	    dialog.setVisible(false);
	    if (option == JFileChooser.APPROVE_OPTION) {
		File f = chooser.getSelectedFile();
		currentDirectory =
		    chooser.getCurrentDirectory().getPath();
		serverInterface.saveGame(f);
	    }
	    synchronized (ServerControlModel.this) {
		dialog = null;
	    }
	}
    }

    private class LoadListener implements ActionListener {
	private JFileChooser chooser;

	public LoadListener(JFileChooser c) {
	    chooser = c;
	}

	public void actionPerformed(ActionEvent e) {
	    int option = JFileChooser.CANCEL_OPTION;
	    if (JFileChooser.APPROVE_SELECTION.equals
		    (e.getActionCommand())) {
		option = JFileChooser.APPROVE_OPTION;
	    }
	    dialog.setVisible(false);
	    if (option == JFileChooser.APPROVE_OPTION) {
		File f = chooser.getSelectedFile();
		if (! f.isFile()) {
		    modelRoot.getUserMessageLogger().println("You must "
			    + "select a valid file name");
		    return;
		}
		currentDirectory =
		    chooser.getCurrentDirectory().getPath();
		serverInterface.loadGame(f);
	    }
	    synchronized (ServerControlModel.this) {
		dialog = null;
	    }
	}
    }

    private class LoadGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
	    synchronized (ServerControlModel.this) {
		if (dialog != null)
		    return;

		if (serverInterface != null) {
		    JFileChooser chooser = new JFileChooser(currentDirectory);
		    chooser.setMultiSelectionEnabled(false);
		    int option;
		    LoadListener listener = new LoadListener(chooser);
		    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		    chooser.addActionListener(listener);
		    dialog = screenHandler.showDialog(chooser,
			    chooser.getDialogTitle());
		}
	    }
        }

        public LoadGameAction() {
            putValue(NAME, "Load Game");
            putValue(MNEMONIC_KEY, new Integer(76));
        }
    }

    private Action loadGameAction = new LoadGameAction();

    private class SaveGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
	    synchronized (ServerControlModel.this) {
		if (dialog != null)
		    return;

		if (serverInterface != null) {
		    JFileChooser chooser = new JFileChooser(currentDirectory);
		    chooser.setMultiSelectionEnabled(false);
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    int option;
		    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		    SaveListener listener = new SaveListener(chooser);
		    chooser.addActionListener(listener);
		    dialog = screenHandler.showDialog(chooser,
			    chooser.getDialogTitle());
		}
	    }
	}

        public SaveGameAction() {
            putValue(NAME, "Save Game");
            putValue(MNEMONIC_KEY, new Integer(83));
        }
    }

    private Action saveGameAction = new SaveGameAction();

    private class SetTargetTicksPerSecondAction extends AbstractAction {
        protected int speed;

        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                serverInterface.setTargetTicksPerSecond(speed);
            }
        }

        public SetTargetTicksPerSecondAction(String name, int speed) {
            putValue(NAME, name);
            this.speed = speed;
        }
    }

    private ActionAdapter targetTicksPerSecondActions = new ActionAdapter(new Action[] {
                new SetTargetTicksPerSecondAction("Pause", 0),
                new SetTargetTicksPerSecondAction("Slow", 10),
                new SetTargetTicksPerSecondAction("Moderate", 30),
                new SetTargetTicksPerSecondAction("Fast", 50),
                

            /* TODO one day we will make turbo faster :) */
            new SetTargetTicksPerSecondAction("Turbo", 50)
            }, 1);

    public void setServerControlInterface(ServerControlInterface i) {
        serverInterface = i;

        boolean enabled = (serverInterface != null);
        loadGameAction.setEnabled(enabled);
        saveGameAction.setEnabled(enabled);

        Enumeration e = targetTicksPerSecondActions.getActions();

        while (e.hasMoreElements()) {
            ((Action)e.nextElement()).setEnabled(enabled);
        }

        if (i == null) {
            selectMapActions = new ActionAdapter(new Action[0]);
        } else {
            String[] mapNames = i.getMapNames();
            Action[] actions = new Action[mapNames.length];

            for (int j = 0; j < actions.length; j++) {
                actions[j] = new NewGameAction(mapNames[j]);
                actions[j].setEnabled(enabled);
            }

            selectMapActions = new ActionAdapter(actions);
        }

        newGameAction.setEnabled(enabled);

	if (modelRoot.getWorld() != null) {
	    GameCalendar gc = (GameCalendar)
		modelRoot.getWorld().get(ITEM.CALENDAR, Player.AUTHORITATIVE);
	    int ticks = gc.getTicksPerSecond();
	    if (ticks == 0) {
		targetTicksPerSecondActions.setSelectedItem("Pause");
	    } else if (ticks <= 10) {
		targetTicksPerSecondActions.setSelectedItem("Slow");
	    } else if (ticks <= 30) {
		targetTicksPerSecondActions.setSelectedItem("Moderate");
	    } else if (ticks <= 50) {
		targetTicksPerSecondActions.setSelectedItem("Fast");
	    }
	}
    }

    public ServerControlModel(ServerControlInterface i, ModelRoot mr) {
	modelRoot = mr;
        setServerControlInterface(i);
    }

    /**
     * @return an action to load a game.
     * TODO The action produces a file selector dialog to load the game
     */
    public Action getLoadGameAction() {
        return loadGameAction;
    }

    /**
     * @return an action to save a game
     * TODO The action produces a file selector dialog to save the game
     */
    public Action getSaveGameAction() {
        return saveGameAction;
    }

    /**
     * @return an action adapter to set the target ticks per second
     */
    public ActionAdapter getSetTargetTickPerSecondActions() {
        return targetTicksPerSecondActions;
    }

    /**
     * When calling this action, set the action command string to the desired
     * map name, or call the appropriate selectMapAction.
     * @return an action to start a new game
     */
    public Action getNewGameAction() {
        return newGameAction;
    }

    /**
     * @return an ActionAdapter representing a list of actions representing
     * valid map names
     */
    public ActionAdapter getMapNames() {
        return selectMapActions;
    }

    public void setScreenHandler(ScreenHandler sh) {
	screenHandler = sh;
    }
}
