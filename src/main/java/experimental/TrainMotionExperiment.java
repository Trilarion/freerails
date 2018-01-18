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

package experimental;

import freerails.client.ClientConfig;
import freerails.client.GameLoop;
import freerails.client.ScreenHandler;
import freerails.client.common.ModelRootImpl;
import freerails.controller.*;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.util.ImmutableList;
import freerails.util.LineSegment;
import freerails.util.Point2D;
import freerails.world.ActivityIterator;
import freerails.world.World;
import freerails.world.WorldConstants;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.track.NullTrackType;
import freerails.world.track.PathIterator;
import freerails.world.train.*;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Random;

// TODO Update the trains position when necessary. Make the train stop at intervals, and slowly accelerate.
/**
 * A visual test for the train movement code.
 */
class TrainMotionExperiment extends JComponent {

    private static final long serialVersionUID = 3690191057862473264L;
    private final World world;
    private final FreerailsPrincipal principal;
    private double finishTime = 0;
    private long startTime;

    /**
     *
     */
    private TrainMotionExperiment() {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        TrackMoveProducer producer = new TrackMoveProducer(me, world, mr);
        TileTransition[] trackPath = {TileTransition.EAST, TileTransition.SOUTH_EAST, TileTransition.SOUTH, TileTransition.SOUTH_WEST, TileTransition.WEST, TileTransition.NORTH_WEST, TileTransition.NORTH, TileTransition.NORTH_EAST};
        Point2D from = new Point2D(5, 5);
        MoveStatus ms = producer.buildTrack(from, trackPath);
        if (!ms.status) throw new IllegalStateException(ms.message);

        TrainOrdersModel[] orders = {};
        ImmutableSchedule is = new ImmutableSchedule(orders, -1, false);
        PreMove addTrain = new AddTrainPreMove(0, new ImmutableList<>(), from, principal, is);

        Move m = addTrain.generateMove(world);
        ms = m.doMove(world, principal);
        if (!ms.status) throw new IllegalStateException(ms.message);

        startTime = System.currentTimeMillis();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("SHOWFPS", "true");

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.getContentPane().add(new TrainMotionExperiment());

        ScreenHandler screenHandler = new ScreenHandler(f, ClientConfig.WINDOWED_MODE);
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
        for (int x = 0; x < world.getMapWidth(); x++) {
            for (int y = 0; y < world.getMapHeight(); y++) {
                FullTerrainTile tile = (FullTerrainTile) world.getTile(x, y);
                if (tile.getTrackPiece().getTrackTypeID() != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
                    g.drawRect(x * WorldConstants.TILE_SIZE, y * WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);

                }
            }
        }

        long l = System.currentTimeMillis() - startTime;

        double ticks = (double) l / 1000;

        while (ticks > finishTime) {

            updateTrainPosition();

        }

        ActivityIterator ai = world.getActivities(principal, 0);
        while (ai.getFinishTime() < ticks && ai.hasNext()) {
            ai.nextActivity();
        }
        double t = Math.min(ticks, ai.getFinishTime());
        t = t - ai.getStartTime();

        TrainMotion motion = (TrainMotion) ai.getActivity();

        TrainPositionOnMap pos = (TrainPositionOnMap) ai.getState(ticks);

        PathOnTiles pathOT = motion.getPath();
        Iterator<Point2D> it = pathOT.tiles();
        while (it.hasNext()) {
            Point2D tile = it.next();
            int x = tile.x * WorldConstants.TILE_SIZE;
            int y = tile.y * WorldConstants.TILE_SIZE;
            int w = WorldConstants.TILE_SIZE;
            int h = WorldConstants.TILE_SIZE;
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w, h);
        }

        pathOT = motion.getTiles(t);
        it = pathOT.tiles();
        while (it.hasNext()) {
            Point2D tile = it.next();
            int x = tile.x * WorldConstants.TILE_SIZE;
            int y = tile.y * WorldConstants.TILE_SIZE;
            int w = WorldConstants.TILE_SIZE;
            int h = WorldConstants.TILE_SIZE;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w, h);
        }

        g.setColor(Color.BLACK);
        LineSegment line = new LineSegment();
        PathIterator path = pos.path();
        while (path.hasNext()) {
            path.nextSegment(line);
            g.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        }

        int speed = (int) Math.round(pos.getSpeed());
        g.drawString("Speed: " + speed, 260, 60);

    }

    private void updateTrainPosition() {
        Random rand = new Random(System.currentTimeMillis());
        MoveTrainPreMove moveTrain = new MoveTrainPreMove(0, principal, new OccupiedTracks(principal, world));
        Move move;
        if (rand.nextInt(10) == 0) {
            move = moveTrain.stopTrain(world);
        } else {
            move = moveTrain.generateMove(world);
        }
        MoveStatus moveStatus = move.doMove(world, principal);
        if (!moveStatus.status) throw new IllegalStateException(moveStatus.message);

        ActivityIterator ai = world.getActivities(principal, 0);

        while (ai.hasNext()) {
            ai.nextActivity();
            finishTime = ai.getFinishTime();
        }
    }
}