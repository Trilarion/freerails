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

package org.railz.client.view;

import java.awt.event.*;
import javax.swing.*;

import org.railz.util.*;

public class HelpMenu extends JMenu {
    GUIRoot guiRoot;

    public HelpMenu (GUIRoot gr) {
        super(Resources.get("Help"));
	guiRoot = gr;

        JMenuItem about = new JMenuItem(Resources.get("About"));
        about.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showAbout();
                }
            });

        JMenuItem how2play = new JMenuItem(Resources.get("Getting started"));
        how2play.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiRoot.getDialogueBoxController();
			if (dbc != null)
			    dbc.showHow2Play();
                }
            });

        JMenuItem showControls = new JMenuItem
	    (Resources.get("Show game controls"));
        showControls.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showGameControls();
                }
            });

	JMenuItem modInfo = new JMenuItem(Resources.get("Modding Info"));
	modInfo.addActionListener
	    (new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null) {
			dbc.showContent(new ModInfoPanel(guiRoot));
		    }
		}
	     });

        add(showControls);
        add(how2play);
        add(about);
	add(modInfo);
    }

}

