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
    private ToggleActionAdapter clientMoveDebugModel;

    private class ClientMoveDebugAction extends AbstractAction {
	public ClientMoveDebugAction() {
	    putValue(NAME, Resources.get("Client move debug"));
	    setEnabled(false);
	    putValue("Selected", Boolean.FALSE);
	}

	public void actionPerformed(ActionEvent e) {
	    if (Boolean.FALSE == getValue("Selected")) {
		putValue("Selected", Boolean.TRUE);
	    } else {
		putValue("Selected", Boolean.FALSE);
	    }
	}
    }
    
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
	clientMoveDebugModel = new ToggleActionAdapter(new
		ClientMoveDebugAction());
    }

    public ToggleActionAdapter getClientMoveDebugModel() {
	return clientMoveDebugModel;
    }

    public ToggleActionAdapter getFrameRateDebugModel () {
	return frameRateDebugModel;
    }
}
