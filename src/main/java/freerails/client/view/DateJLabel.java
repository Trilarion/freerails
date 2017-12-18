/*
 * Created on 01-Jun-2003
 *
 */
package freerails.client.view;

import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.top.ITEM;
import freerails.world.top.ReadOnlyWorld;

import javax.swing.*;
import java.awt.*;

/**
 * This JLabel shows the current date.
 *
 * @author Luke
 */
public class DateJLabel extends JLabel implements View {
    private static final long serialVersionUID = 3689348840578757942L;

    private ReadOnlyWorld w;

    /**
     *
     */
    public DateJLabel() {
        this.setText("          ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (null != w) {
            GameTime time = w.currentTime();
            GameCalendar gameCalendar = (GameCalendar) w.get(ITEM.CALENDAR);
            String s = gameCalendar.getYearAndMonth(time.getTicks());
            super.setText(s);
        }

        super.paintComponent(g);
    }

    /**
     *
     * @param model
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot model, RenderersRoot vl, Action closeAction) {
        this.w = model.getWorld();
    }
}