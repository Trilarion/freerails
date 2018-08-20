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

package freerails.client;

import freerails.client.launcher.GameLoop;
import freerails.client.launcher.ScreenHandler;
import freerails.controller.*;
import freerails.model.MapFixtureFactory2;
import freerails.model.train.activity.Activity;
import freerails.model.track.OccupiedTracks;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.motion.TrainPositionOnMap;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.move.generator.MoveGenerator;

import freerails.nove.Status;
import freerails.util.Segment;
import freerails.util.Vec2D;
import freerails.util.BidirectionalIterator;
import freerails.model.world.World;
import freerails.model.ModelConstants;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;
import freerails.model.train.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

// TODO Update the trains position when necessary. Make the train stop at intervals, and slowly accelerate.
/**
 * A visual test for the train movement code.
 */
class TrainMotionExperiment extends JComponent {

    private static final long serialVersionUID = 3690191057862473264L;
    private final World world;
    private final Player player;
    private double finishTime = 0;
    private long startTime;

    /**
     *
     */
    private TrainMotionExperiment() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        player = moveExecutor.getPlayer();
        ModelRoot modelRoot = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(moveExecutor, world, modelRoot);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST, TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        Vec2D from = new Vec2D(5, 5);
        Status status = producer.buildTrack(from, trackPath);
        if (!status.isSuccess()) throw new IllegalStateException(status.getMessage());

        TrainOrder[] orders = {};
        UnmodifiableSchedule schedule = new Schedule(orders, -1, false);
        MoveGenerator addTrain = new AddTrainMoveGenerator(0, new ArrayList<>(), from, player, schedule);

        Move move = addTrain.generate(world);
        status = move.doMove(world, player);
        if (!status.isSuccess()) throw new IllegalStateException(status.getMessage());

        startTime = System.currentTimeMillis();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.getContentPane().add(new TrainMotionExperiment());

        ScreenHandler screenHandler = new ScreenHandler(f, ClientConstants.WINDOWED_MODE);
        screenHandler.apply();

        GameLoop gameLoop = new GameLoop(screenHandler);
        Thread t = new Thread(gameLoop);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Shade tiles with track..
        g.setColor(Color.GREEN);
        for (int x = 0; x < world.getMapSize().x; x++) {
            for (int y = 0; y < world.getMapSize().y; y++) {
                TerrainTile tile = world.getTile(new Vec2D(x, y));
                if (tile.getTrackPiece() != null) {
                    g.drawRect(x * ModelConstants.TILE_SIZE, y * ModelConstants.TILE_SIZE, ModelConstants.TILE_SIZE, ModelConstants.TILE_SIZE);
                }
            }
        }

        long l = System.currentTimeMillis() - startTime;

        double ticks = (double) l / 1000;

        while (ticks > finishTime) {

            updateTrainPosition();
        }

        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, 0).getActivities();
        while (bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < ticks && bidirectionalIterator.hasNext()) {
            bidirectionalIterator.next();
        }
        double t = Math.min(ticks, bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration());
        t = t - bidirectionalIterator.get().getStartTime();

        TrainMotion motion = (TrainMotion) bidirectionalIterator.get();

        /** Converts an absolute time value to a time value relative to the start of
        * the current activity. If absoluteTime is greater then getFinishTime(), getDuration() is
        * returned. */
        double dt = ticks - bidirectionalIterator.get().getStartTime();
        dt = Math.min(dt, bidirectionalIterator.get().getDuration());

        TrainPositionOnMap pos = (TrainPositionOnMap) bidirectionalIterator.get().getStateAtTime(dt);

        PathOnTiles pathOT = motion.getPath();
        Iterator<Vec2D> it = pathOT.tilesIterator();
        while (it.hasNext()) {
            Vec2D tile = it.next();
            int x = tile.x * ModelConstants.TILE_SIZE;
            int y = tile.y * ModelConstants.TILE_SIZE;
            int w = ModelConstants.TILE_SIZE;
            int h = ModelConstants.TILE_SIZE;
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w, h);
        }

        pathOT = motion.getTiles(t);
        it = pathOT.tilesIterator();
        while (it.hasNext()) {
            Vec2D tile = it.next();
            int x = tile.x * ModelConstants.TILE_SIZE;
            int y = tile.y * ModelConstants.TILE_SIZE;
            int w = ModelConstants.TILE_SIZE;
            int h = ModelConstants.TILE_SIZE;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w, h);
        }

        g.setColor(Color.BLACK);
        Segment line = null;
        PathIterator path = pos.path();
        while (path.hasNext()) {
            line = path.nextSegment();
            g.drawLine(line.getA().x, line.getA().y, line.getB().x, line.getB().y);
        }

        int speed = (int) Math.round(pos.getSpeed());
        g.drawString("Speed: " + speed, 260, 60);
    }

    private void updateTrainPosition() {
        Random rand = new Random(System.currentTimeMillis());
        MoveTrainMoveGenerator moveTrain = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world));
        Move move;
        if (rand.nextInt(10) == 0) {
            move = moveTrain.stopTrain(world);
        } else {
            move = moveTrain.generate(world);
        }
        Status status = move.doMove(world, player);
        if (!status.isSuccess()) throw new IllegalStateException(status.getMessage());

        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, 0).getActivities();

        while (bidirectionalIterator.hasNext()) {
            bidirectionalIterator.next();
            finishTime = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration();
        }
    }
}