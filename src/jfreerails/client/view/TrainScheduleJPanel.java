package jfreerails.client.view;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.TrainImages;
import jfreerails.client.renderer.ViewLists;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
/**
 *  This JPanel displays a train's schedule and provides controls that let you edit it.
 * @author  Luke Lindsay
 */
public class TrainScheduleJPanel extends javax.swing.JPanel implements View, WorldListListener {
    
    private int trainNumber = -1;
    
    private int scheduleID = -1;
    
    private TrainOrdersListModel listModel;
    
    private ModelRoot modelRoot;
    
    private  ViewLists vl;
    
    public TrainScheduleJPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        trainOrderJPanel1 = new jfreerails.client.view.TrainOrderJPanel();
        editOrderJPopupMenu = new javax.swing.JPopupMenu();
        gotoStationJMenuItem = new javax.swing.JMenuItem();
        changeStation = new javax.swing.JMenuItem();
        removeStationJMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        addWagonJMenu = new javax.swing.JMenu();
        removeWagonsJMenu = new javax.swing.JMenu();
        removeLastJMenuItem = new javax.swing.JMenuItem();
        removeAllJMenuItem = new javax.swing.JMenuItem();
        changeConsistJMenu = new javax.swing.JMenu();
        noChangeJMenuItem = new javax.swing.JMenuItem();
        engineOnlyJMenuItem = new javax.swing.JMenuItem();
        autoConsistJMenuItem = new javax.swing.JMenuItem();
        waitJMenu = new javax.swing.JMenu();
        dontWaitJMenuItem = new javax.swing.JMenuItem();
        waitUntilFullJMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        pullUpJMenuItem = new javax.swing.JMenuItem();
        pushDownJMenuItem = new javax.swing.JMenuItem();
        selectStationJPanel1 = new jfreerails.client.view.SelectStationJPanel();
        selectStationJPopupMenu = new javax.swing.JPopupMenu();
        this.selectStationJPopupMenu.add(selectStationJPanel1);
        addStationJButton = new javax.swing.JButton();
        priorityOrdersJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        orders = new javax.swing.JList();

        gotoStationJMenuItem.setText("Goto station");
        gotoStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(gotoStationJMenuItem);

        changeStation.setText("Change Station");
        changeStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeStationActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(changeStation);

        removeStationJMenuItem.setText("Remove station");
        removeStationJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeStationJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(removeStationJMenuItem);

        editOrderJPopupMenu.add(jSeparator1);

        addWagonJMenu.setText("Add Wagon");
        editOrderJPopupMenu.add(addWagonJMenu);

        removeWagonsJMenu.setText("Remove wagon(s)");
        removeLastJMenuItem.setText("Remove last");
        removeLastJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLastJMenuItemActionPerformed(evt);
            }
        });

        removeWagonsJMenu.add(removeLastJMenuItem);

        removeAllJMenuItem.setText("Remove all wagons");
        removeAllJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllJMenuItemActionPerformed(evt);
            }
        });

        removeWagonsJMenu.add(removeAllJMenuItem);

        editOrderJPopupMenu.add(removeWagonsJMenu);

        changeConsistJMenu.setText("Change consist to..");
        noChangeJMenuItem.setText("'No change'");
        noChangeJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noChangeJMenuItemActionPerformed(evt);
            }
        });

        changeConsistJMenu.add(noChangeJMenuItem);

        engineOnlyJMenuItem.setText("Engine only");
        engineOnlyJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                engineOnlyJMenuItemActionPerformed(evt);
            }
        });

        changeConsistJMenu.add(engineOnlyJMenuItem);

        autoConsistJMenuItem.setText("Choose wagons automatically");
        autoConsistJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoConsistJMenuItemActionPerformed(evt);
            }
        });

        changeConsistJMenu.add(autoConsistJMenuItem);

        editOrderJPopupMenu.add(changeConsistJMenu);

        waitJMenu.setText("Wait at station");
        dontWaitJMenuItem.setText("Don't wait");
        dontWaitJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontWaitJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(dontWaitJMenuItem);

        waitUntilFullJMenuItem.setText("Wait until full");
        waitUntilFullJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waitUntilFullJMenuItemActionPerformed(evt);
            }
        });

        waitJMenu.add(waitUntilFullJMenuItem);

        editOrderJPopupMenu.add(waitJMenu);

        editOrderJPopupMenu.add(jSeparator2);

        pullUpJMenuItem.setText("Pull up");
        pullUpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pullUpJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pullUpJMenuItem);

        pushDownJMenuItem.setText("Push down");
        pushDownJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pushDownJMenuItemActionPerformed(evt);
            }
        });

        editOrderJPopupMenu.add(pushDownJMenuItem);

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder("Schedule"));
        addStationJButton.setText("Add Station");
        addStationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStationJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(addStationJButton, gridBagConstraints);

        priorityOrdersJButton.setText("Add Priority Orders");
        priorityOrdersJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityOrdersJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(priorityOrdersJButton, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(280, 160));
        orders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        orders.setCellRenderer(trainOrderJPanel1);
        orders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ordersMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(orders);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents

    private void autoConsistJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoConsistJMenuItemActionPerformed
        setAutoConsist();    
    }//GEN-LAST:event_autoConsistJMenuItemActionPerformed
    
    private void changeStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStationActionPerformed
        int orderNumber = this.orders.getSelectedIndex();
        showSelectStation(this.getSchedule(), orderNumber);
    }//GEN-LAST:event_changeStationActionPerformed
    
    private void removeAllJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllJMenuItemActionPerformed
        removeAllWagons();
    }//GEN-LAST:event_removeAllJMenuItemActionPerformed
    
    private void removeLastJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLastJMenuItemActionPerformed
        removeLastWagon();
    }//GEN-LAST:event_removeLastJMenuItemActionPerformed
    
    private void waitUntilFullJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waitUntilFullJMenuItemActionPerformed
        setWaitUntilFull(true);
    }//GEN-LAST:event_waitUntilFullJMenuItemActionPerformed
    
    private void dontWaitJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontWaitJMenuItemActionPerformed
        setWaitUntilFull(false);
    }//GEN-LAST:event_dontWaitJMenuItemActionPerformed
    
    private void engineOnlyJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_engineOnlyJMenuItemActionPerformed
        removeAllWagons();
    }//GEN-LAST:event_engineOnlyJMenuItemActionPerformed
    
    private void noChangeJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noChangeJMenuItemActionPerformed
        noChange();      
    }//GEN-LAST:event_noChangeJMenuItemActionPerformed
    
    private void priorityOrdersJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityOrdersJButtonActionPerformed
        MutableSchedule s = getSchedule();
        s.setPriorityOrders(new TrainOrdersModel(0, null, false, false));
        showSelectStation(s, Schedule.PRIORITY_ORDERS);
    }//GEN-LAST:event_priorityOrdersJButtonActionPerformed
    
    private void addStationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStationJButtonActionPerformed
        MutableSchedule s = getSchedule();
        int newOrderNumber = s.addOrder(new TrainOrdersModel(0, null, false, false));
        showSelectStation(s, newOrderNumber);
    }//GEN-LAST:event_addStationJButtonActionPerformed
    
    private void removeStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeStationJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.removeOrder(i);
        sendUpdateMove(s);
    }//GEN-LAST:event_removeStationJMenuItemActionPerformed
    
    private void gotoStationJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoStationJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.setOrderToGoto(i);
        sendUpdateMove(s);
    }//GEN-LAST:event_gotoStationJMenuItemActionPerformed
    
    private void pushDownJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushDownJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.pushDown(i);
        sendUpdateMove(s);
        orders.setSelectedIndex(i+1);
    }//GEN-LAST:event_pushDownJMenuItemActionPerformed
    
    private void ordersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ordersMouseClicked
        int i = orders.getSelectedIndex();
        MutableSchedule s = getSchedule();
        if(i >= s.getNumOrders()){
            //The selected index does not exist!
            //For some reason, the JList hasn't updated yet.
            i = -1;
        }
        if(-1 != i  && java.awt.event.MouseEvent.BUTTON3 == evt.getButton()){
            //If an element is select and the right button is pressed.
            TrainOrdersModel order = s.getOrder(i);
            pullUpJMenuItem.setEnabled(s.canPullUp(i));
            pushDownJMenuItem.setEnabled(s.canPushDown(i));
            gotoStationJMenuItem.setEnabled(s.canSetGotoStation(i));
            removeWagonsJMenu.setEnabled(order.orderHasWagons());
            waitJMenu.setEnabled(order.orderHasWagons());
            addWagonJMenu.setEnabled(order.hasLessThanMaxiumNumberOfWagons());
            setupWagonsPopup();
            this.editOrderJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_ordersMouseClicked
    
    private void pullUpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullUpJMenuItemActionPerformed
        MutableSchedule s = getSchedule();
        int i = orders.getSelectedIndex();
        s.pullUp(i);
        sendUpdateMove(s);
        orders.setSelectedIndex(i-1);
    }//GEN-LAST:event_pullUpJMenuItemActionPerformed
    
    public void setup(ModelRoot mr,  ViewLists vl, ActionListener al) {
        trainOrderJPanel1.setup(mr, vl, null);
        this.modelRoot = mr;
        this.vl = vl;
        
        //This actionListener is fired by the select station popup when a station is selected.
        ActionListener actionListener =  new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                sendUpdateMove(selectStationJPanel1.generateNewSchedule());
                selectStationJPopupMenu.setVisible(false);
            }
        };
        this.selectStationJPanel1.setup(mr, vl, actionListener);
    }
    
    public void display(int newTrainNumber){
        this.trainNumber = newTrainNumber;
        FreerailsPrincipal principal = modelRoot.getPrincipal();
        ReadOnlyWorld w = modelRoot.getWorld();
        TrainModel train = (TrainModel) w.get(KEY.TRAINS, newTrainNumber, principal);
        this.scheduleID = train.getScheduleID();
        listModel = new TrainOrdersListModel(w, newTrainNumber, principal);
        orders.setModel(listModel);
        orders.setFixedCellWidth(250);
        listModel.fireRefresh();
        enableButtons();
    }
    
    private void enableButtons(){
        MutableSchedule s  = getSchedule();
        addStationJButton.setEnabled(s.canAddOrder());
        
        //Only one set of prority orders are allowed.
        priorityOrdersJButton.setEnabled(!s.hasPriorityOrders());
    }
    
    private MutableSchedule getSchedule(){
        FreerailsPrincipal principal = modelRoot.getPrincipal();
        ReadOnlyWorld w = modelRoot.getWorld();
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, principal);
        ImmutableSchedule immutableSchedule = (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES, train.getScheduleID(), principal);
        return new MutableSchedule(immutableSchedule);
    }
    
    private void setupWagonsPopup() {
        addWagonJMenu.removeAll(); //Remove existing menu items.
        NonNullElements cargoTypes = new NonNullElements(SKEY.CARGO_TYPES, modelRoot.getWorld());
        
        TrainImages trainImages = vl.getTrainImages();
        
        while (cargoTypes.next()) {
            final CargoType wagonType = (CargoType) cargoTypes.getElement();
            JMenuItem wagonMenuItem = new JMenuItem();
            final int wagonTypeNumber = cargoTypes.getIndex();
            wagonMenuItem.setText(wagonType.getDisplayName());
            Image image = trainImages.getSideOnWagonImage(wagonTypeNumber);
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            int scale = height/10;
            ImageIcon icon = new ImageIcon(image.getScaledInstance(width/scale,
            height/scale, Image.SCALE_FAST));
            wagonMenuItem.setIcon(icon);
            wagonMenuItem
            .addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    
                    addWagon(wagonTypeNumber);
                }
            });
            addWagonJMenu.add(wagonMenuItem);
        }
    }
    
    private void noChange(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), null, false, false);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void setWaitUntilFull(boolean b){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        boolean autoConsist = b ? false: oldOrders.autoConsist;
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), oldOrders.consist, b, autoConsist);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
      private void setAutoConsist(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);       
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), null, false, true);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void  addWagon(int wagonTypeNumber){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        int[] newConsist;
        //The consist will be null if old orders were 'no change'.
        if(null != oldOrders.consist){
            int oldLength = oldOrders.consist.length;
            newConsist = new int[oldLength+1];
            //Copy existing wagons
            for( int i = 0 ; i < oldLength ; i ++){
                newConsist[i] = oldOrders.consist[i];
            }
            //Then add specified wagon.
            newConsist[oldLength] = wagonTypeNumber;
        }else{
            newConsist = new int[]{wagonTypeNumber};
        }
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), newConsist, oldOrders.getWaitUntilFull(), false);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void removeAllWagons(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), new int[0], false, false);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void removeLastWagon(){
        TrainOrdersModel oldOrders, newOrders;
        MutableSchedule s = getSchedule();
        int orderNumber = this.orders.getSelectedIndex();
        oldOrders = s.getOrder(orderNumber);
        int[] oldConsist = oldOrders.consist;
        int newLength = oldConsist.length -1 ;
        if(newLength < 0){
            throw new NoSuchElementException("No wagons to remove!");
        }
        int[] newConsist = new int[newLength];
        
        //Copy existing wagons
        System.arraycopy(oldConsist, 0, newConsist, 0, newConsist.length);
        newOrders = new TrainOrdersModel(oldOrders.getStationID(), newConsist, oldOrders.waitUntilFull, false);
        s.setOrder(orderNumber, newOrders);
        sendUpdateMove(s);
    }
    
    private void sendUpdateMove(MutableSchedule mutableSchedule ){
        FreerailsPrincipal principal = modelRoot.getPrincipal();
        ReadOnlyWorld w = modelRoot.getWorld();
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, this.trainNumber, principal);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule before = (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES, scheduleID, principal);
        ImmutableSchedule after = mutableSchedule.toImmutableSchedule();
        Move m = new ChangeTrainScheduleMove(scheduleID, before, after, principal);
        this.modelRoot.doMove(m);
    }
    
    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if(KEY.TRAIN_SCHEDULES == key && this.scheduleID == index){
            listModel.fireRefresh();
            enableButtons();
        }
    }
    
    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
        //do nothing.
    }
    
    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
        //do nothing.
    }
    
    /** Show the popup that lets the user select a station, called when a new
     * scheduled stop is added and when an existing scheduled stop is changed.*/
    private void showSelectStation(MutableSchedule schedule, int orderNumber){
        selectStationJPanel1.display(schedule, orderNumber);
        
        //Show the select station popup in the middle of the window.
        Container topLevelAncestor = this.getTopLevelAncestor();
        Dimension d = topLevelAncestor.getSize();
        Dimension d2 = selectStationJPopupMenu.getPreferredSize();
        int x = Math.max((d.width - d2.width)/2, 0);
        int y = Math.max((d.height - d2.height)/2, 0);
        selectStationJPopupMenu.show(topLevelAncestor, x, y);
        selectStationJPanel1.requestFocus();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addStationJButton;
    private javax.swing.JMenu addWagonJMenu;
    private javax.swing.JMenuItem autoConsistJMenuItem;
    private javax.swing.JMenu changeConsistJMenu;
    private javax.swing.JMenuItem changeStation;
    private javax.swing.JMenuItem dontWaitJMenuItem;
    private javax.swing.JPopupMenu editOrderJPopupMenu;
    private javax.swing.JMenuItem engineOnlyJMenuItem;
    private javax.swing.JMenuItem gotoStationJMenuItem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem noChangeJMenuItem;
    private javax.swing.JList orders;
    private javax.swing.JButton priorityOrdersJButton;
    private javax.swing.JMenuItem pullUpJMenuItem;
    private javax.swing.JMenuItem pushDownJMenuItem;
    private javax.swing.JMenuItem removeAllJMenuItem;
    private javax.swing.JMenuItem removeLastJMenuItem;
    private javax.swing.JMenuItem removeStationJMenuItem;
    private javax.swing.JMenu removeWagonsJMenu;
    private jfreerails.client.view.SelectStationJPanel selectStationJPanel1;
    private javax.swing.JPopupMenu selectStationJPopupMenu;
    private jfreerails.client.view.TrainOrderJPanel trainOrderJPanel1;
    private javax.swing.JMenu waitJMenu;
    private javax.swing.JMenuItem waitUntilFullJMenuItem;
    // End of variables declaration//GEN-END:variables
    
}
