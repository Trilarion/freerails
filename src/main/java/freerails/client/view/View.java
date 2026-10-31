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

/*
 *
 */
package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;

import javax.swing.*;

/**
 * Defines a standard method to initiate GUI components that need access to the
 * ModelRoot.
 */
public interface View {

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param rendererRoot the renderer root parameter
     * @param closeAction the action invoked when the panel is closed
     */

    void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction);
}