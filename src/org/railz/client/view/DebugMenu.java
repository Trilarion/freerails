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

import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;

import org.railz.util.Resources;
import org.railz.client.model.ModelRoot;
import org.railz.client.model.DebugModel;

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
	mi = new
	    JCheckBoxMenuItem(debugModel.getClientMoveDebugModel().getAction());
	mi.setModel(debugModel.getClientMoveDebugModel());
	add(mi);
    }
}
