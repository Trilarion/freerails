/*
 * TrainView.java
 *
 * Created on 22 August 2003, 20:49
 */
package jfreerails.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;


/**
 *  This JList displays an engine and a number of wagons.
 * @author  Luke Lindsay
 */
public class TrainViewJList extends JList implements View, ListCellRenderer {
    private ReadOnlyWorld w;
    private TrainConsistListModel trainConsistListModel;
    private final WagonView wagonView;
    private FreerailsPrincipal principal;

    public TrainViewJList(ModelRoot mr, ViewLists vl, int trainNumber) {
        wagonView = new WagonView();
        setup(mr, vl, null);
        this.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        this.setVisibleRowCount(1);

        display(trainNumber);

        this.setCellRenderer(wagonView);

        this.setBackground(Color.GRAY);
    }

    private void display(int trainNumber) {
        trainConsistListModel = new TrainConsistListModel(w, trainNumber,
                principal);
        this.setModel(trainConsistListModel);
    }

    public void setup(ModelRoot mr, ViewLists vl,
        ActionListener submitButtonCallBack) {
        this.w = mr.getWorld();
        wagonView.setup(mr, vl, null);
        principal = mr.getPrincipal();
    }

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        display(index);

        return this;
    }
}