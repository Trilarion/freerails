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

import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.economy.transaction.Transaction;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.util.Vector2D;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.TransactionCategory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Arrays;

/**
 * A JPopupMenu that displays the list of industries that can be built. This
 * class contains the code that generates and dispatches a ChangeTileMove when
 * the player clicks on the menu.
 */
public class BuildIndustryPopupMenu extends JPopupMenu implements View {

    @Serial
    private static final long serialVersionUID = 3689636912575165749L;
    private Vector2D location;

    /**
     * Sets the cursor location.
     *
     * @param p the map coordinate
     */
    public void setCursorLocation(Vector2D p) {
        location = p;
    }

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param rendererRoot the renderer root parameter
     * @param closeAction the action invoked when the panel is closed
     */
    @Override
    public void setup(final ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        removeAll();

        for (Terrain terrain: modelRoot.getWorld().getTerrains()) {
            final Money price = terrain.getIndustryBuildCost();

            if (null != price) {
                JMenuItem item = new JMenuItem("Build " + terrain.getName() + " ($" + price + ")");
                item.addActionListener(new ActionListener() {
                    private final int terrainId = terrain.getId();

                    /**
                     * Handles the specified action event.
                     * @param e the e
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        Move move = new ChangeTileMove(new TerrainTile(terrainId, modelRoot.getWorld().getTile(location).trackPiece()), location);
                        Transaction transaction = new ItemTransaction(TransactionCategory.INDUSTRIES, Money.opposite(price), modelRoot.getWorld().getClock().getCurrentTime(), 1, terrainId);
                        Move m2 = new AddTransactionMove(modelRoot.getPlayer(), transaction);
                        Move m3 = new CompostMove(Arrays.asList(move, m2));
                        Status status = modelRoot.applyMove(m3);

                        if (!status.isSuccess()) {
                            modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, status.getMessage());
                        }
                    }
                });
                add(item);
            }
        }
    }
}