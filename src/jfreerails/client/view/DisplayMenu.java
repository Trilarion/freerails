package jfreerails.client.view;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class DisplayMenu extends JMenu {
    private GUIRoot guiRoot;

    public DisplayMenu(GUIRoot gcf) {
        super("Display");
	guiRoot = gcf;
        setMnemonic(68);

        JMenuItem trainOrdersJMenuItem = new JMenuItem("Train Orders");
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainOrders();
                }
            });

        JMenuItem stationInfoJMenuItem = new JMenuItem("Station Info");
        stationInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showStationInfo(0);
                }
            });

        JMenuItem trainListJMenuItem = new JMenuItem("Train List");
        trainListJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainList();
                }
            });

        JMenuItem profitLossJMenuItem = new JMenuItem("Profit and Loss"
		+ " Statement");
        profitLossJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showProfitLoss();
                }
            });

        add(trainOrdersJMenuItem);
        add(stationInfoJMenuItem);
        add(trainListJMenuItem);
	add(profitLossJMenuItem);
    }

}
