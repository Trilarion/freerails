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
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;


/**
 *  This JList displays an engine and a number of wagons.
 * @author  Luke Lindsay
 */
public class TrainViewJList extends JList implements View, ListCellRenderer {
    private ReadOnlyWorld w;
    private TrainConsistListModel trainConsistListModel;
    private WagonView wagonView;
    private FreerailsPrincipal principal;

    /** Creates a new instance of TrainView */
    public TrainViewJList(ModelRoot mr, int trainNumber) {
        wagonView = new WagonView();
        setup(mr, null);
        this.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        this.setVisibleRowCount(1);

        display(trainNumber);

        this.setCellRenderer(wagonView);

        this.setBackground(Color.GRAY);
    }

    public void display(int trainNumber) {
        trainConsistListModel = new TrainConsistListModel(w, trainNumber,
                principal);
        this.setModel(trainConsistListModel);
    }

    public void setup(ModelRoot mr, ActionListener submitButtonCallBack) {
        this.w = mr.getWorld();
        wagonView.setup(mr, null);
        principal = mr.getPlayerPrincipal();
    }

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        display(index);

        return this;
    }
}