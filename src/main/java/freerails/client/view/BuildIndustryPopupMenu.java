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
 *
 */
package freerails.client.view;

import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;
import freerails.model.finance.transaction.Transaction;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.util.Vec2D;
import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.TransactionCategory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * A JPopupMenu that displays the list of industries that can be built. This
 * class contains the code that generates and dispatches a ChangeTileMove when
 * the player clicks on the menu.
 */
public class BuildIndustryPopupMenu extends JPopupMenu implements View {

    private static final long serialVersionUID = 3689636912575165749L;
    private Vec2D location;

    /**
     * @param p
     */
    public void setCursorLocation(Vec2D p) {
        location = p;
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    @Override
    public void setup(final ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        removeAll();

        for (Terrain terrain: modelRoot.getWorld().getTerrains()) {
            final Money price = terrain.getBuildCost();

            if (null != price) {
                JMenuItem item = new JMenuItem(terrain.getName() + ' ' + price);
                item.addActionListener(new ActionListener() {
                    private final int terrainId = terrain.getId();

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        Move move = new ChangeTileMove(new TerrainTile(terrainId, modelRoot.getWorld().getTile(location).getTrackPiece()), location);
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