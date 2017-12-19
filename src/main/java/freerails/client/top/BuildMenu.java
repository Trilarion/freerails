/*
 * BuildMenu.java
 *
 * Created on 30 July 2001, 06:49
 */
package freerails.client.top;

import freerails.client.view.ActionRoot;

/**
 * The menu that lets you select a track type.
 *
 */
final public class BuildMenu extends javax.swing.JMenu {
    private static final long serialVersionUID = 3617850859305055542L;

    /**
     *
     */
    public BuildMenu() {
        super();
    }

    /**
     *
     * @param actionRoot
     */
    public void setup(ActionRoot actionRoot) {
        this.removeAll();
        this.setText("Build");

        add(actionRoot.getBuildTrainDialogAction());
    }
}