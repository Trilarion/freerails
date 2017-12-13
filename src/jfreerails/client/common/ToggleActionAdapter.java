package jfreerails.client.common;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JToggleButton.ToggleButtonModel;

/**
 * Provides an action adapter with a ToggleButtonModel model
 */
public class ToggleActionAdapter extends ToggleButtonModel {
    private Action action;

    public ToggleActionAdapter (Action a) {
	action = a;
    }

    public void setSelected(boolean s) {
	super.setSelected(s);

	action.actionPerformed(new ActionEvent(this,
		    ActionEvent.ACTION_PERFORMED, (String)
		    action.getValue(Action.ACTION_COMMAND_KEY)));
    }

    public Action getAction() {
	return action;
    }
}
