/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * BuildMenu.java
 *
 */
package freerails.client.top;

import freerails.client.view.ActionRoot;

/**
 * The menu that lets you select a track type.
 */
public final class BuildMenu extends javax.swing.JMenu {
    private static final long serialVersionUID = 3617850859305055542L;

    /**
     *
     */
    public BuildMenu() {
        super();
    }

    /**
     * @param actionRoot
     */
    public void setup(ActionRoot actionRoot) {
        this.removeAll();
        this.setText("Build");

        add(actionRoot.getBuildTrainDialogAction());
    }
}