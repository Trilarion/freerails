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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConfig;
import freerails.model.WorldConstants;
import freerails.util.ui.Painter;
import freerails.util.ui.SoundManager;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainRenderer;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.train.TrainAccessor;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.train.Train;
import freerails.model.train.TrainPositionOnMap;

import java.awt.*;

/**
 * Draws the trains on the main map.
 */
public class OverHeadTrainView implements Painter {

    private final TrainRenderer trainPainter;
    private final ReadOnlyWorld world;
    private final SoundManager soundManager = SoundManager.getInstance();
    private final ModelRoot modelRoot;

    /**
     * @param world
     * @param rendererRoot
     * @param modelRoot
     */
    public OverHeadTrainView(ReadOnlyWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.world = world;
        trainPainter = new TrainRenderer(rendererRoot);
        this.modelRoot = modelRoot;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(10));

        Double time = (Double) modelRoot.getProperty(ModelRootProperty.TIME);

        for (int k = 0; k < world.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();

            for (int i = 0; i < world.size(principal, PlayerKey.Trains); i++) {
                Train train = (Train) world.get(principal, PlayerKey.Trains, i);

                // TrainPositionOnMap pos = (TrainPositionOnMap) world.get(
                // principal, KEY.TRAIN_POSITIONS, i);
                TrainAccessor ta = new TrainAccessor(world, principal, i);
                TrainPositionOnMap pos = ta.findPosition(time, newVisibleRectangle);
                if (pos == null) continue;
                if (TrainPositionOnMap.isCrashSite() && (TrainPositionOnMap.getFrameCt() <= WorldConstants.TRAIN_CRASH_FRAMES_COUNT)) {
                    // TODO reimplement trainPainter.paintTrainCrash(g, pos);
                    if (TrainPositionOnMap.getFrameCt() == 1) {
                            soundManager.playSound(ClientConfig.SOUND_TRAIN_CRASH, 1);
                    }
                } else {
                    trainPainter.paintTrain(g, train, pos);
                }
            }
        }
    }
}