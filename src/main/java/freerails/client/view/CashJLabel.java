/*
 * Created on 01-Jun-2003
 *
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the amount of cash available.
 *
 * @author Luke
 */
public class CashJLabel extends JLabel implements View {
    private static final long serialVersionUID = 3257853181542412341L;

    private ReadOnlyWorld w;

    private FreerailsPrincipal principal;

    public CashJLabel() {
        this.setText("          ");
    }

    public void setup(ModelRoot model, RenderersRoot vl, Action closeAction) {
        this.w = model.getWorld();
        principal = model.getPrincipal();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != w) {
            Money m = w.getCurrentBalance(principal);
            String s = m.toString();
            this.setText("$" + s);
        }

        super.paintComponent(g);
    }
}