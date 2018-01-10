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

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.common.Painter;
import freerails.client.common.SoundManager;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainRenderer;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.TrainAccessor;
import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainPositionOnMap;

import java.awt.*;

/**
 * Draws the trains on the main map.
 */
public class OverHeadTrainView implements Painter {
    private final TrainRenderer trainPainter;

    private final ReadOnlyWorld w;

    private final SoundManager soundManager = SoundManager.getSoundManager();

    private final ModelRoot mr;

    /**
     * @param world
     * @param rr
     * @param mr
     */
    public OverHeadTrainView(ReadOnlyWorld world, RendererRoot rr, ModelRoot mr) {
        w = world;
        trainPainter = new TrainRenderer(rr);
        this.mr = mr;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(10));

        Double time = (Double) mr.getProperty(Property.TIME);

        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            for (int i = 0; i < w.size(principal, KEY.TRAINS); i++) {
                TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS, i);

                // TrainPositionOnMap pos = (TrainPositionOnMap) w.get(
                // principal, KEY.TRAIN_POSITIONS, i);
                TrainAccessor ta = new TrainAccessor(w, principal, i);
                TrainPositionOnMap pos = ta.findPosition(time,
                        newVisibleRectangle);
                if (pos == null)
                    continue;
                if (pos.isCrashSite()
                        && (pos.getFrameCt() <= TrainPositionOnMap.CRASH_FRAMES_COUNT)) {
                    // TODO reimplement trainPainter.paintTrainCrash(g, pos);
                    if (pos.getFrameCt() == 1) {
                        try {
                            soundManager.playSound(
                                    ClientConfig.SOUND_TRAIN_CRASH, 1);
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    trainPainter.paintTrain(g, train, pos);
                }
            }
        }
    }
}