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

package jfreerails.client.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.util.Enumeration;

import jfreerails.client.common.ActionAdapter;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.model.ServerControlModel;

public class GameMenu extends JMenu {
    private ServerControlModel sc;
    private GUIRoot guiRoot;

    public GameMenu (ModelRoot mr, GUIRoot gr) {
	super ("Game");
	guiRoot = gr;
	sc = mr.getServerControls();

	setMnemonic(71);

	JMenuItem quitJMenuItem = new JMenuItem("Exit Game");
	quitJMenuItem.setMnemonic(88);

	quitJMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		// TODO exit properly
		System.exit(0);
		}
		});

	final JMenu newGameJMenu = new JMenu(sc.getNewGameAction());
	newGameJMenu.addMenuListener(new MenuListener() {
		public void menuSelected(MenuEvent e) {
		newGameJMenu.removeAll();

		Enumeration actions = sc.getMapNames().getActions();
		ButtonGroup bg = new ButtonGroup();

		while (actions.hasMoreElements()) {
		JMenuItem mi = new JMenuItem((Action)actions.nextElement());
		newGameJMenu.add(mi);
		}
		}

		public void menuCanceled(MenuEvent e) {
		}

		public void menuDeselected(MenuEvent e) {
		}
		});

	JMenuItem saveGameJMenuItem = new JMenuItem(sc.getSaveGameAction());

	JMenuItem loadGameJMenuItem = new JMenuItem(sc.getLoadGameAction());

	JMenuItem newspaperJMenuItem = new JMenuItem("Newspaper");
	newspaperJMenuItem.setMnemonic(78);

	newspaperJMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
			guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showNewspaper("Headline");
		//glassPanel.setVisible(true);
		}
		});

	//Set up the gamespeed submenu.
	ButtonGroup group = new ButtonGroup();
	ActionAdapter speedActions = sc.getSetTargetTickPerSecondActions();
	JMenu gameSpeedSubMenu = new JMenu("Game Speed");

	Enumeration buttonModels = speedActions.getButtonModels();
	Enumeration actions = speedActions.getActions();

	while (buttonModels.hasMoreElements()) {
	    JRadioButtonMenuItem mi = new JRadioButtonMenuItem((Action)actions.nextElement());
	    mi.setModel((ButtonModel)buttonModels.nextElement());
	    group.add(mi);
	    gameSpeedSubMenu.add(mi);
	}

	add(newGameJMenu);
	addSeparator();
	add(loadGameJMenuItem);
	add(saveGameJMenuItem);
	addSeparator();
	add(gameSpeedSubMenu);
	add(newspaperJMenuItem);
	addSeparator();
	add(quitJMenuItem);
    }

    public void setup() {
	sc.setScreenHandler(guiRoot.getScreenHandler());
    }
}
