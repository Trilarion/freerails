/*
 * Created on Sep 9, 2004
 *
 */
package freerails.client.top;

import freerails.client.renderer.RenderersRoot;
import freerails.client.view.View;
import freerails.controller.ModelRoot;
import freerails.move.*;
import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.terrain.TerrainType;
import freerails.world.top.NonNullElements;
import freerails.world.top.SKEY;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A JPopupMenu that displays the list of industries that can be built. This
 * class contains the code that generates and dispatches a ChangeTileMove when
 * the player clicks on the menu.
 *
 * @author Luke
 */
public class BuildIndustryJPopupMenu extends JPopupMenu implements View {
    private static final long serialVersionUID = 3689636912575165749L;

    private final Point cursorLocation = new Point();

    /**
     *
     * @param p
     */
    public void setCusorLocation(Point p) {
        cursorLocation.x = p.x;
        cursorLocation.y = p.y;
    }

    /**
     *
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(final ModelRoot modelRoot, RenderersRoot vl,
                      Action closeAction) {
        this.removeAll();

        final NonNullElements it = new NonNullElements(SKEY.TERRAIN_TYPES,
                modelRoot.getWorld());

        while (it.next()) {
            TerrainType type = (TerrainType) it.getElement();
            final Money price = type.getBuildCost();

            if (null != price) {
                JMenuItem item = new JMenuItem(type.getDisplayName() + " "
                        + price);
                item.addActionListener(new ActionListener() {
                    private final int terrainType = it.getIndex();

                    public void actionPerformed(ActionEvent arg0) {
                        Move m1 = new ChangeTileMove(modelRoot.getWorld(),
                                cursorLocation, terrainType);
                        Transaction t = new AddItemTransaction(
                                Transaction.Category.INDUSTRIES, terrainType,
                                1, price.changeSign());
                        Move m2 = new AddTransactionMove(modelRoot
                                .getPrincipal(), t);
                        CompositeMove m3 = new CompositeMove(m1, m2);
                        MoveStatus ms = modelRoot.doMove(m3);

                        if (!ms.ok) {
                            modelRoot.setProperty(
                                    ModelRoot.Property.CURSOR_MESSAGE,
                                    ms.message);
                        }
                    }
                });
                add(item);
            }
        }
    }
}