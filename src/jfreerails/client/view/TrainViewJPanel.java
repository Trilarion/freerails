/*
 * TrainView.java
 *
 * Created on 22 August 2003, 20:49
 */
package jfreerails.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;


/**
 *  This JPanel displays an engine and a number of wagons.
 * @author  Luke Lindsay
 */
public class TrainViewJPanel extends JPanel implements View, ListCellRenderer,
    WorldListListener {
    private ReadOnlyWorld w;
    private ViewLists vl;
    private int trainNumber = -1;
    private int scheduleOrderNumber;
    private int scheduleID = -1;
    private int height = 100;
    private FreerailsPrincipal principal;
    private Image[] images = new Image[0];

    /** Whether this JPanel should one of the trains orders from the schedule instead of the trains current formation.*/
    private boolean showingOrder = false;

    /** If true, the train is drawn in the center to the JPanel; if false,
     *the train is drawn left aligned.
     */
    private boolean centerTrain = false;
    private int trainWidth = 0;
    private boolean selected = false;
    private final Color backgoundColor = (java.awt.Color)javax.swing.UIManager.getDefaults()
                                                                              .get("List.background");
    private final Color selectedColor = (java.awt.Color)javax.swing.UIManager.getDefaults()
                                                                             .get("List.selectionBackground");

    public TrainViewJPanel() {
        this.setOpaque(false);
    }

    public TrainViewJPanel(ModelRoot mr, ViewLists vl) {
        setup(mr, vl, null);
        this.setBackground(backgoundColor);
    }

    public void setCenterTrain(boolean b) {
        this.centerTrain = b;
    }

    public void display(int trainNumber) {
        showingOrder = false;
        this.trainNumber = trainNumber;

        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, principal);

        //Set up the array of images.
        images = new Image[1 + train.getNumberOfWagons()];
        images[0] = vl.getTrainImages().getSideOnEngineImage(train.getEngineType(),
                height);

        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            images[i + 1] = vl.getTrainImages().getSideOnWagonImage(train.getWagon(
                        i), height);
        }

        resetPreferredSize();
    }

    public void display(int trainNumber, int scheduleOrderNumber) {
        showingOrder = true;
        this.trainNumber = trainNumber;
        this.scheduleOrderNumber = scheduleOrderNumber;

        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, principal);
        this.scheduleID = train.getScheduleID();

        ImmutableSchedule s = (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES,
                scheduleID, principal);
        TrainOrdersModel order = s.getOrder(scheduleOrderNumber);

        //Set up the array of images.
        if (null != order.consist) {
            images = new Image[1 + order.consist.length];
            images[0] = vl.getTrainImages().getSideOnEngineImage(train.getEngineType(),
                    height);

            for (int i = 0; i < order.consist.length; i++) {
                images[i + 1] = vl.getTrainImages().getSideOnWagonImage(order.consist[i],
                        height);
            }
        } else {
            images = new Image[0];
        }

        resetPreferredSize();
    }

    private void resetPreferredSize() {
        int width = 0;

        for (int i = 0; i < images.length; i++) {
            width += images[i].getWidth(null);
        }

        this.trainWidth = width;
        this.setPreferredSize(new Dimension(width, height));
    }

    public void setup(ModelRoot mr, ViewLists vl,
        ActionListener submitButtonCallBack) {
        this.w = mr.getWorld();
        this.vl = vl;
        this.principal = mr.getPrincipal();
    }

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
        display(index);

        if (selected != isSelected) {
            selected = isSelected;

            if (selected) {
                setBackground(selectedColor);
            } else {
                setBackground(backgoundColor);
            }
        }

        return this;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int i) {
        height = i;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 0;

        if (this.centerTrain) {
            x = (this.getWidth() - this.trainWidth) / 2;
        }

        for (int i = 0; i < images.length; i++) {
            g.drawImage(images[i], x, 0, null);
            x += images[i].getWidth(null);
        }
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if (showingOrder) {
            if (KEY.TRAIN_SCHEDULES == key && this.scheduleID == index) {
                this.display(this.trainNumber, this.scheduleOrderNumber);
            }
        } else {
            if (KEY.TRAINS == key && this.trainNumber == index) {
                this.display(this.trainNumber);
            }
        }
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
    }
}