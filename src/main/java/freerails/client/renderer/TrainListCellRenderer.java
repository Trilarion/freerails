/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.model.operations.train.Train;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.operations.train.schedule.TrainOrder;

import javax.swing.*;

import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.util.List;

/**
 * Displays an engine and a number of wagons.
 */
public class TrainListCellRenderer extends JPanel implements View, ListCellRenderer<Train> {

    @Serial
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
     * Creates a train list cell renderer instance.
     */
    public TrainListCellRenderer() {
        setOpaque(false);
    }

    /**
     * Creates a train list cell renderer instance.
     *
     * @param modelRoot the client model that provides current game state
     * @param vl the vl parameter
     */
    public TrainListCellRenderer(ModelRoot modelRoot, RendererRoot vl) {
        setup(modelRoot, vl, null);
        setBackground(backgoundColor);
    }

    /**
     * Sets the center train.
     *
     * @param b the button or flag parameter
     */
    public void setCenterTrain(boolean b) {
        centerTrain = b;
    }

    /**
     * Performs the display operation.
     *
     * @param newTrainNumber the new train number parameter
     */
    public void display(int newTrainNumber) {
        showingOrder = false;
        trainNumber = newTrainNumber;

        Train train = world.getTrain(player, trainNumber);
        display(train.getEngine(), train.getConsist());
        resetPreferredSize();
    }

    /**
     * Display.
     * @param engine the engine
     * @param wagons the wagons
     */
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
     * Performs the display operation.
     *
     * @param newTrainNumber the new train number parameter
     * @param newScheduleOrderID the new schedule order id parameter
     */
    public void display(int newTrainNumber, int newScheduleOrderID) {
        showingOrder = true;
        trainNumber = newTrainNumber;
        scheduleOrderNumber = newScheduleOrderID;

        Train train =  world.getTrain(player, trainNumber);
        UnmodifiableSchedule schedule = train.getSchedule();
        TrainOrder order = schedule.getOrder(newScheduleOrderID);

        // Set up the array of images.
        if (order.hasSpecifiedConsist()) {
            display(train.getEngine(), order.getConsist());
        } else {
            images = new Image[0];
        }

        resetPreferredSize();
    }

    /**
     * Reset preferred size.
     */
    private void resetPreferredSize() {
        int width = 0;

        for (Image image : images) {
            width += image.getWidth(null);
        }

        trainWidth = width;
        setPreferredSize(new Dimension(width, height));
    }

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param rendererRoot the renderer root parameter
     * @param closeAction the action invoked when the panel is closed
     */
    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        world = modelRoot.getWorld();
        vl = rendererRoot;
        player = modelRoot.getPlayer();
    }

    /**
     * Returns the list cell renderer component.
     * @param list the list
     * @param value the value
     * @param trainId the train id
     * @param isSelected the is selected
     * @param cellHasFocus the cell has focus
     * @return the list cell renderer component
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends Train> list, Train value, int trainId, boolean isSelected, boolean cellHasFocus) {

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

    /**
     * Returns the height.
     * @return the height
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param i the index of the item
     */
    public void setHeight(int i) {
        height = i;
    }

    /**
     * Paints this component.
     * @param g the g
     */
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

    // TODO since Schedule is part of train, this is not right anymore, fix it
    // TODO since we use AddTrainMove this is not done right anymore (no updates)! fix it!
}