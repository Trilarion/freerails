/*
* BuildMenu.java
*
* Created on 30 July 2001, 06:49
*/
package jfreerails.client.top;

import javax.swing.ButtonGroup;

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

        add(actionRoot.getBuildTrainDialogAction());
    }
}