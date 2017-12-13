/*
 * Copyright (C) Luke Lindsay
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

/*
* BuildMenu.java
*
* Created on 30 July 2001, 06:49
*/
package org.railz.client.top;

import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButtonMenuItem;
import org.railz.client.common.ActionAdapter;
import org.railz.client.model.ModelRoot;
import org.railz.client.model.TrackBuildModel;
import org.railz.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class BuildMenu extends javax.swing.JMenu {
    private ButtonGroup buttonGroup;
    private ButtonGroup buttonGroup2;
    private TrackBuildModel trackBuildModel;

    /** Creates new BuildMenu */
    public BuildMenu() {
        super();
    }

    public void setup(ModelRoot modelRoot) {
        this.removeAll();
        this.setText("Build");
        buttonGroup = new ButtonGroup();
        trackBuildModel = modelRoot.getTrackBuildModel();

        ActionAdapter actionAdapter = trackBuildModel.getBuildModeActionAdapter();

        Enumeration e = actionAdapter.getActions();
        Enumeration buttonModels = actionAdapter.getButtonModels();

        while (e.hasMoreElements()) {
            Action action = (Action)e.nextElement();
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
            menuItem.setModel((ButtonModel)buttonModels.nextElement());
            buttonGroup.add(menuItem);
            add(menuItem);
        }

        buttonGroup2 = new ButtonGroup();

        this.addSeparator();

        actionAdapter = trackBuildModel.getTrackRuleAdapter();

        e = actionAdapter.getActions();
        buttonModels = actionAdapter.getButtonModels();

        while (e.hasMoreElements()) {
            Action action = (Action)e.nextElement();
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(action);
            menuItem.setIcon(null);
            menuItem.setModel((ButtonModel)buttonModels.nextElement());
            buttonGroup2.add(menuItem);
            add(menuItem);
        }

        addSeparator();
        add(modelRoot.getBuildTrainDialogAction());
    }
}
