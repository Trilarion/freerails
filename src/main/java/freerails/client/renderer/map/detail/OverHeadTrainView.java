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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConstants;
import freerails.client.renderer.map.MapCoordinateAdapter;
import freerails.model.ModelConstants;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.util.BidirectionalIterator;
import freerails.util.IntPoint2D;
import freerails.util.ui.Painter;
import freerails.util.ui.SoundManager;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainRenderer;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.train.motion.TrainPositionOnMap;

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
     * Creates a over head train view instance.
     *
     * @param world the game world being queried
     * @param rendererRoot the renderer root parameter
     * @param modelRoot the client model that provides current game state
     */
    public OverHeadTrainView(UnmodifiableWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        this.world = world;
        trainPainter = new TrainRenderer(rendererRoot);
        this.modelRoot = modelRoot;
    }

    /**
     * Performs the paint operation.
     *
     * @param g the graphics context used for drawing
     * @param newVisibleRectangle the new visible rectangle parameter
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
                if (TrainPositionOnMap.isCrashSite() && TrainPositionOnMap.getFrameCt() <= ModelConstants.TRAIN_CRASH_FRAMES_COUNT) {
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
     * Performs the find position operation.
     *
     * @param time the time parameter
     * @param view the view parameter
     * @return the position produced by this method
     */
    public static TrainPositionOnMap findPosition(double time, Rectangle view, UnmodifiableWorld world, Player player, int trainId) {
        BidirectionalIterator<TrainMotion> bidirectionalIterator = world.getTrain(player, trainId).getActivities();

        // TODO why starting at the end and going backwards?
        // goto last
        bidirectionalIterator.gotoLast();
        TrainMotion trainMotion = bidirectionalIterator.previous();
        // search backwards
        while (trainMotion.getStartTime() + trainMotion.getDuration() >= time && bidirectionalIterator.hasPrevious()) {
            trainMotion = bidirectionalIterator.previous();
        }
        boolean afterFinish = trainMotion.getStartTime() + trainMotion.getDuration() < time;
        while (afterFinish && bidirectionalIterator.hasNext()) {
            trainMotion = bidirectionalIterator.next();
            afterFinish = trainMotion.getStartTime() + trainMotion.getDuration() < time;
        }
        double dt = time - trainMotion.getStartTime();
        dt = Math.min(dt, trainMotion.getDuration());

        IntPoint2D start = trainMotion.getPath().getStart();
        int trainLength = trainMotion.getTrainLength();
        IntPoint2D startPixels = MapCoordinateAdapter.tileOrigin(start);
        int trainLengthPixels = MapCoordinateAdapter.modelDistanceToPixel(trainLength);
        Rectangle trainBox = new Rectangle(
                startPixels.x() - trainLengthPixels * 2,
                startPixels.y() - trainLengthPixels * 2,
                trainLengthPixels * 4,
                trainLengthPixels * 4);
        if (!view.intersects(trainBox)) {
            return null; // TODO what does that mean
        }
        return trainMotion.getStateAtTime(dt);
    }

}