package jfreerails.client.model;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import jfreerails.util.Resources;
import jfreerails.client.common.ToggleActionAdapter;

/**
 * Control models for the debug menu
 */
public class DebugModel {
    private ToggleActionAdapter frameRateDebugModel;

    private class FrameRateDebugAction extends AbstractAction {
	public FrameRateDebugAction() {
	    putValue(NAME, Resources.get("Show Frame Rate"));
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
	    // ignore
	}
    }

    DebugModel() {
	frameRateDebugModel = new ToggleActionAdapter
	    (new FrameRateDebugAction());
    }

    public ToggleActionAdapter getFrameRateDebugModel () {
	return frameRateDebugModel;
    }
}
