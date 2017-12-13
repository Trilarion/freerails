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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class HelpMenu extends JMenu {
    GUIRoot guiRoot;

    public HelpMenu (GUIRoot gr) {
        super("Help");
	guiRoot = gr;

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showAbout();
                }
            });

        JMenuItem how2play = new JMenuItem("Getting started");
        how2play.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiRoot.getDialogueBoxController();
			if (dbc != null)
			    dbc.showHow2Play();
                }
            });

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
		    DialogueBoxController dbc =
			guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showGameControls();
                }
            });

        add(showControls);
        add(how2play);
        add(about);
    }

}

