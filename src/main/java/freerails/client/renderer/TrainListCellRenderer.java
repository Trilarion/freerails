/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * TrainView.java
 *
 */
package freerails.client.renderer;

import freerails.client.view.View;
import freerails.client.ModelRoot;
import freerails.model.train.Train;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.WorldListListener;
import freerails.model.player.Player;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.train.schedule.TrainOrder;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Displays an engine and a number of wagons.
 */
public class TrainListCellRenderer extends JPanel implements View, ListCellRenderer, WorldListListener {

    private static final long serialVersionUID = 3546076964969591093L;
    private final Color backgoundColor = (Color) UIManager.getDefaults().get("List.background");
    private final Color selectedColor = (Color) UIManager.getDefaults().get("List.selectionBackground");
    private final Color selectedColorNotFocused = Color.LIGHT_GRAY;
    private UnmodifiableWorld world;
    private RendererRoot vl;
    private int trainNumber = -1;
    private int scheduleOrderNumber;
    private int scheduleID = -1;
    private int height = 100;
    private Player player;
    private Image[] images = new Image[0];
    /**
     * Whether this JPanel should one of the trains orders from the schedule
     * instead of the trains current formation.
     */
    private boolean showingOrder = false;
    /**
     * If true, the train is drawn in the center to the JPanel; if false, the
     * train is drawn left aligned.
     */
    private boolean centerTrain = false;
    private int trainWidth = 0;

    /**
     *
     */
    public TrainListCellRenderer() {
        setOpaque(false);
    }

    /**
     * @param modelRoot
     * @param vl
     */
    public TrainListCellRenderer(ModelRoot modelRoot, RendererRoot vl) {
        setup(modelRoot, vl, null);
        setBackground(backgoundColor);
    }

    /**
     * @param b
     */
    public void setCenterTrain(boolean b) {
        centerTrain = b;
    }

    /**
     * @param newTrainNumber
     */
    public void display(int newTrainNumber) {
        showingOrder = false;
        trainNumber = newTrainNumber;

        Train train = world.getTrain(player, trainNumber);
        display(train.getEngine(), train.getConsist());
        resetPreferredSize();
    }

    private void display(int engine, List<Integer> wagons) {
        images = new Image[1 + wagons.size()];
        // images[0] = vl.getTrainImages().getSideOnEngineImage(
        // train.getEngineId(), height);
        String engineFilename = vl.getEngineImages(engine).sideOnFileName;
        try {
            images[0] = vl.getScaledImage(engineFilename, height);
        } catch (IOException e) {
            throw new IllegalArgumentException(engineFilename);
        }
        for (int i = 0; i < wagons.size(); i++) {
            // images[i + 1] = vl.getTrainImages().getSideOnWagonImage(
            // order.consist.get(i), height);
            int wagonType = wagons.get(i);
            String wagonFilename = vl.getWagonImages(wagonType).sideOnFileName;
            try {
                images[i + 1] = vl.getScaledImage(wagonFilename, height);
            } catch (IOException e) {
                throw new IllegalArgumentException(wagonFilename);
            }
        }
    }

    /**
     * @param newTrainNumber
     * @param newScheduleOrderID
     */
    public void display(int newTrainNumber, int newScheduleOrderID) {
        showingOrder = true;
        trainNumber = newTrainNumber;
        scheduleOrderNumber = newScheduleOrderID;

        Train train =  world.getTrain(player, trainNumber);
        UnmodifiableSchedule schedule = train.getSchedule();
        TrainOrder order = schedule.getOrder(newScheduleOrderID);

        // Set up the array of images.
        if (null != order.getConsist()) {
            display(train.getEngine(), order.getConsist());
        } else {
            images = new Image[0];
        }

        resetPreferredSize();
    }

    private void resetPreferredSize() {
        int width = 0;

        for (Image image : images) {
            width += image.getWidth(null);
        }

        trainWidth = width;
        setPreferredSize(new Dimension(width, height));
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
        this.vl = rendererRoot;
        player = modelRoot.getPlayer();
    }

    public Component getListCellRendererComponent(JList list, Object value, int trainId, boolean isSelected, boolean cellHasFocus) {

        // TODO this is probably supposed to be the index.th entry of a list
        display(trainId);

        if (isSelected) {
            if (list.isFocusOwner()) {
                setBackground(selectedColor);
            } else {
                setBackground(selectedColorNotFocused);
            }
        } else {
            setBackground(backgoundColor);
        }

        return this;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * @param i
     */
    public void setHeight(int i) {
        height = i;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 0;

        if (centerTrain) {
            x = (getWidth() - trainWidth) / 2;
        }

        for (Image image : images) {
            g.drawImage(image, x, 0, null);
            x += image.getWidth(null);
        }
    }

    /**
     * @param index
     * @param player
     */
    public void listUpdated(int index, Player player) {
        if (showingOrder) {
            // TODO since Schedule is part of train, this is not right anymore, fix it
            //if (PlayerKey.TrainSchedules == key) {
            //    display(trainNumber, scheduleOrderNumber);
            //}
        } else {
            // TODO since we use AddTrainMove this is not done right anymore! fix it!
            //if (PlayerKey.Trains == key && trainNumber == index) {
            //    display(trainNumber);
            //}
        }
    }

    /**
     * @param index
     * @param player
     */
    public void itemAdded(int index, Player player) {
    }

    /**
     * @param index
     * @param player
     */
    public void itemRemoved(int index, Player player) {
    }
}