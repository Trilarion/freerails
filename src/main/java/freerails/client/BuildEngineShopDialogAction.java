/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.client;

import javax.swing.*;
import java.awt.event.ActionEvent;

class BuildEngineShopDialogAction extends AbstractAction {

    private final ActionRoot actionRoot;

    BuildEngineShopDialogAction(ActionRoot actionRoot) {
        super("Build Engine Shop");
        this.actionRoot = actionRoot;
        putValue(SHORT_DESCRIPTION, "Build an engine shop at a station");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (actionRoot.getDialogueBoxController() != null) {
            actionRoot.getDialogueBoxController().showBuildEngineShop();
        }
    }
}
