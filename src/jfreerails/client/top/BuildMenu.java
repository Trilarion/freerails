/*
* BuildMenu.java
*
* Created on 30 July 2001, 06:49
*/
package jfreerails.client.top;

import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButtonMenuItem;
import jfreerails.client.common.ActionAdapter;
import jfreerails.client.view.ActionRoot;
import jfreerails.client.view.TrackBuildModel;


/**
* The menu that lets you select a track type.
* @author  Luke Lindsay
*/
final public class BuildMenu extends javax.swing.JMenu {
    private ButtonGroup buttonGroup;
    private ButtonGroup buttonGroup2;
    private TrackBuildModel trackBuildModel;

    public BuildMenu() {
        super();
    }

    public void setup(ActionRoot actionRoot) {
        this.removeAll();
        this.setText("Build");
        buttonGroup = new ButtonGroup();
        trackBuildModel = actionRoot.getTrackBuildModel();

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
        add(actionRoot.getBuildTrainDialogAction());
    }
}