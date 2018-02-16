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
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.util.Vector2D;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.SharedKey;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.terrain.TerrainType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A JPopupMenu that displays the list of industries that can be built. This
 * class contains the code that generates and dispatches a ChangeTileMove when
 * the player clicks on the menu.
 */
public class BuildIndustryPopupMenu extends JPopupMenu implements View {

    private static final long serialVersionUID = 3689636912575165749L;
    private Vector2D cursorLocation;

    /**
     * @param p
     */
    public void setCursorLocation(Vector2D p) {
        cursorLocation = p;
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(final ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        removeAll();

        final NonNullElementWorldIterator it = new NonNullElementWorldIterator(SharedKey.TerrainTypes, modelRoot.getWorld());

        while (it.next()) {
            TerrainType type = (TerrainType) it.getElement();
            final Money price = type.getBuildCost();

            if (null != price) {
                JMenuItem item = new JMenuItem(type.getDisplayName() + ' ' + price);
                item.addActionListener(new ActionListener() {
                    private final int terrainType = it.getIndex();

                    public void actionPerformed(ActionEvent e) {
                        Move move = new ChangeTileMove(modelRoot.getWorld(), cursorLocation, terrainType);
                        Transaction transaction = new ItemTransaction(TransactionCategory.INDUSTRIES, terrainType, 1, Money.opposite(price));
                        Move m2 = new AddTransactionMove(modelRoot.getPrincipal(), transaction);
                        Move m3 = new CompositeMove(move, m2);
                        MoveStatus moveStatus = modelRoot.doMove(m3);

                        if (!moveStatus.succeeds()) {
                            modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, moveStatus.getMessage());
                        }
                    }
                });
                add(item);
            }
        }
    }
}