/*
 * Created on 01-Jun-2003
 *
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * This JLabel shows the amount of cash available.
 * @author Luke
 *
 */
public class CashJLabel extends JLabel implements View {
    private ReadOnlyWorld w;
    private FreerailsPrincipal principal;

    public CashJLabel() {
        this.setText("         ");
    }

    public void setup(ModelRoot model, ViewLists vl,
        ActionListener submitButtonCallBack) {
        this.w = model.getWorld();
        principal = model.getPrincipal();
    }

    protected void paintComponent(Graphics g) {
        if (null != w) {
            Money m = w.getCurrentBalance(principal);
            String s = m.toString();
            this.setText(s);
        }

        super.paintComponent(g);
    }
}