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

package freerails.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 *
 */
class BuildTrainDialogAction extends AbstractAction {
    private static final long serialVersionUID = 3257853173002416948L;
    private ActionRoot actionRoot;

    BuildTrainDialogAction(ActionRoot actionRoot) {
        super("Build Train");
        this.actionRoot = actionRoot;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        putValue(SHORT_DESCRIPTION, "Build a new train");
    }

    public void actionPerformed(ActionEvent e) {
        if (actionRoot.getDialogueBoxController() != null) {
            actionRoot.getDialogueBoxController().showSelectEngine();
        }
    }
}
