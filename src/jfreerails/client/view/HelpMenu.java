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

