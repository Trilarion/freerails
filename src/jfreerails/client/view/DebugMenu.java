package jfreerails.client.view;

import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;

import jfreerails.util.Resources;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.model.DebugModel;

/**
 * Debug menu which provides ability to turn on and off various debug info
 */
public class DebugMenu extends JMenu {
    public DebugMenu(ModelRoot mr) {
	super(Resources.get("Debug"));
	DebugModel debugModel = mr.getDebugModel();
	JCheckBoxMenuItem mi = new JCheckBoxMenuItem(debugModel
		.getFrameRateDebugModel().getAction());
	mi.setModel(debugModel.getFrameRateDebugModel());
	add(mi);
    }
}
