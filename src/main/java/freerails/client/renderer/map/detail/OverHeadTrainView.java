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

import freerails.client.ClientConstants;
import freerails.model.ModelConstants;
import freerails.model.train.Train;
import freerails.model.train.activity.Activity;
import freerails.model.train.motion.TrainMotion;
import freerails.util.BidirectionalIterator;
import freerails.util.Vec2D;
import freerails.util.ui.Painter;
import freerails.util.ui.SoundManager;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainRenderer;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.train.motion.TrainPositionOnMap;

import java.awt.*;

/**
 * Draws the trains on the main map.
 */
public class OverHeadTrainView implements Painter {

    private final TrainRenderer trainPainter;
    private final UnmodifiableWorld world;
    private final SoundManager soundManager = SoundManager.getInstance();
    private final ModelRoot modelRoot;

    /**
     * @param world
     * @param rendererRoot
     * @param modelRoot
     */
    public OverHeadTrainView(UnmodifiableWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.world = world;
        trainPainter = new TrainRenderer(rendererRoot);
        this.modelRoot = modelRoot;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    @Override
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(10));

        Double time = (Double) modelRoot.getProperty(ModelRootProperty.TIME);

        for (Player player : world.getPlayers()) {
            for (Train train: world.getTrains(player)) {
                TrainPositionOnMap pos = findPosition(time, newVisibleRectangle, world, player, train.getId());
                if (pos == null) continue;
                if (TrainPositionOnMap.isCrashSite() && (TrainPositionOnMap.getFrameCt() <= ModelConstants.TRAIN_CRASH_FRAMES_COUNT)) {
                    // TODO reimplement trainPainter.paintTrainCrash(g, pos);
                    if (TrainPositionOnMap.getFrameCt() == 1) {
                        soundManager.playSound(ClientConstants.SOUND_TRAIN_CRASH, 1);
                    }
                } else {
                    trainPainter.paintTrain(g, train, pos);
                }
            }
        }
    }


    /**
     * @param time
     * @param view
     * @return
     */
    public static TrainPositionOnMap findPosition(double time, Rectangle view, UnmodifiableWorld world, Player player, int trainId) {
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, trainId).getActivities();

        // TODO why starting at the end and going backwards?
        // goto last
        bidirectionalIterator.gotoLast();
        // search backwards
        while (bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() >= time && bidirectionalIterator.hasPrevious()) {
            bidirectionalIterator.previous();
        }
        boolean afterFinish = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < time;
        while (afterFinish && bidirectionalIterator.hasNext()) {
            bidirectionalIterator.next();
            afterFinish = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < time;
        }
        double dt = time - bidirectionalIterator.get().getStartTime();
        dt = Math.min(dt, bidirectionalIterator.get().getDuration());
        TrainMotion trainMotion = (TrainMotion) bidirectionalIterator.get();

        Vec2D start = trainMotion.getPath().getStart();
        int trainLength = trainMotion.getTrainLength();
        Rectangle trainBox = new Rectangle(start.x * ModelConstants.TILE_SIZE - trainLength * 2, start.y * ModelConstants.TILE_SIZE - trainLength * 2, trainLength * 4, trainLength * 4);
        if (!view.intersects(trainBox)) {
            return null; // TODO doesn't work
        }
        return trainMotion.getStateAtTime(dt);
    }

}