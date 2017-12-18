/*
 * TrainOrdersListModel.java
 *
 * Created on 23 August 2003, 17:49
 */
package freerails.client.view;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.train.ImmutableSchedule;
import freerails.world.train.Schedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;

import javax.swing.*;

/**
 * AbstractListModel used by {@link TrainScheduleJPanel} to display the orders
 * making up a train schedule.
 *
 * @author Luke Lindsay
 */
public class TrainOrdersListModel extends AbstractListModel {
    public static final int DONT_GOTO = 0;
    public static final int GOTO_NOW = 1;
    public static final int GOTO_AFTER_PRIORITY_ORDERS = 2;
    private static final long serialVersionUID = 3762537827703009847L;
    private final int trainNumber;
    private final ReadOnlyWorld w;
    private final FreerailsPrincipal principal;

    public TrainOrdersListModel(ReadOnlyWorld w, int trainNumber,
                                FreerailsPrincipal p) {
        this.trainNumber = trainNumber;
        this.w = w;
        this.principal = p;
        assert (null != getSchedule());
    }

    public Object getElementAt(int index) {
        Schedule s = getSchedule();
        int gotoStatus;

        if (s.getNextScheduledOrder() == index) {
            if (s.hasPriorityOrders()) {
                gotoStatus = GOTO_AFTER_PRIORITY_ORDERS;
            } else {
                gotoStatus = GOTO_NOW;
            }
        } else {
            if (s.hasPriorityOrders() && 0 == index) {
                // These orders are the priority orders.
                gotoStatus = GOTO_NOW;
            } else {
                gotoStatus = DONT_GOTO;
            }
        }

        boolean isPriorityOrders = 0 == index && s.hasPriorityOrders();
        TrainOrdersModel order = getSchedule().getOrder(index);

        return new TrainOrdersListElement(isPriorityOrders, gotoStatus, order,
                trainNumber);
    }

    public int getSize() {
        Schedule s = getSchedule();
        int size = 0;
        if (s != null) {
            size = s.getNumOrders();
        }
        return size;
    }

    public void fireRefresh() {
        super.fireContentsChanged(this, 0, getSize());
    }

    private Schedule getSchedule() {
        TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS,
                trainNumber);
        ImmutableSchedule sched = null;
        if (train != null) {
            sched = (ImmutableSchedule) w.get(principal, KEY.TRAIN_SCHEDULES,
                    train.getScheduleID());
        }
        return sched;
    }

    /**
     * This class holds the values that are needed by the ListCellRender.
     * TrainOrdersListModel.getElementAt(int index) returns an instance of this
     * class.
     */
    public static class TrainOrdersListElement {
        public final boolean isPriorityOrder;

        public final int gotoStatus;

        public final TrainOrdersModel order;

        public final int trainNumber;

        public TrainOrdersListElement(boolean isPriorityOrder, int gotoStatus,
                                      TrainOrdersModel order, int trainNumber) {
            this.isPriorityOrder = isPriorityOrder;
            this.gotoStatus = gotoStatus;
            this.order = order;
            this.trainNumber = trainNumber;
        }
    }
}