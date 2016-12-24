/*
 * Created on Sep 9, 2004
 *
 */
package jfreerails.client.top;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jfreerails.client.renderer.RenderersRoot;
import jfreerails.client.view.View;
import jfreerails.controller.ModelRoot;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.ChangeTileMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.SKEY;

/**
 * A JPopupMenu that displays the list of industries that can be built. This
 * class contains the code that generates and dispatches a ChangeTileMove when
 * the player clicks on the menu.
 * 
 * @author Luke
 * 
 */
public class BuildIndustryJPopupMenu extends JPopupMenu implements View {
    private static final long serialVersionUID = 3689636912575165749L;

    private final Point cursorLocation = new Point();

    public void setCusorLocation(Point p) {
        cursorLocation.x = p.x;
        cursorLocation.y = p.y;
    }

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