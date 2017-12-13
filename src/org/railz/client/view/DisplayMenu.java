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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.railz.util.Resources;
import org.railz.client.model.*;

public class DisplayMenu extends JMenu {
    private GUIRoot guiRoot;
    private ModelRoot modelRoot;

    public DisplayMenu(GUIRoot gcf, ModelRoot mr) {
        super(Resources.get("Display"));
	modelRoot = mr;
	guiRoot = gcf;
        setMnemonic(68);

        JMenuItem trainOrdersJMenuItem = new JMenuItem
	    (Resources.get("Train Orders"));
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainOrders();
                }
            });

        JMenuItem stationInfoJMenuItem = new JMenuItem
	    (Resources.get("Station Supply and Demand"));
        stationInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showStationInfo(0);
                }
            });

        JMenuItem trainListJMenuItem = new JMenuItem
	    (Resources.get("Train List"));
        trainListJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showTrainList();
                }
            });

        JMenuItem profitLossJMenuItem = new JMenuItem
	    (Resources.get("Profit and Loss Statement"));
        profitLossJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showProfitLoss();
                }
            });

        JMenuItem balanceSheetJMenuItem = new JMenuItem
	    (Resources.get("Balance Sheet"));
        balanceSheetJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showBalanceSheet();
                }
            });

        JMenuItem gameInfoJMenuItem = new JMenuItem
	    (Resources.get("Game Info"));
        gameInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
		    DialogueBoxController dbc =
		    guiRoot.getDialogueBoxController();
		    if (dbc != null)
			dbc.showContent(new GameInfo(modelRoot));
                }
            });
	
        add(trainOrdersJMenuItem);
        add(stationInfoJMenuItem);
        add(trainListJMenuItem);
	add(profitLossJMenuItem);
	add(balanceSheetJMenuItem);
	add(gameInfoJMenuItem);
    }

}
