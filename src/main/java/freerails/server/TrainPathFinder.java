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

package freerails.server;

import freerails.controller.*;
import freerails.network.MoveReceiver;
import freerails.util.ImPoint;
import freerails.util.IntIterator;
import freerails.world.PositionOnTrack;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldDiffs;
import freerails.world.player.FreerailsPrincipal;

/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints, it also deals with stops at
 * stations.
 */
public class TrainPathFinder implements IntIterator, ServerAutomaton {

    private static final long serialVersionUID = 3256446893302559280L;
    final ReadOnlyWorld w;
    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final TrainStopsHandler stopsHandler;
    private final FlatTrackExplorer trackExplorer;
    private transient MoveReceiver mr = null;

    /**
     * @param tx
     * @param w
     * @param trainNumber
     * @param newMr
     * @param p
     */
    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
                           int trainNumber, MoveReceiver newMr, FreerailsPrincipal p) {
        this.trackExplorer = tx;
        stopsHandler = new TrainStopsHandler(trainNumber, p,
                new WorldDiffs(w));
        this.mr = newMr;
        this.w = w;
    }

    /**
     * @return
     */
    public boolean hasNextInt() {

        boolean moving = stopsHandler.isTrainMoving();

        if (moving) {
            return trackExplorer.hasNextEdge();
        }
        mr.process(stopsHandler.getMoves());
        return false;
    }

    public void initAutomaton(MoveReceiver newMr) {
        this.mr = newMr;
    }

    boolean isTrainMoving() {

        boolean moving = stopsHandler.isTrainMoving();

        mr.process(stopsHandler.getMoves());
        return moving;
    }

    /**
     * @return a PositionOnTrack packed into an int
     */
    public int nextInt() {

        PositionOnTrack tempP = new PositionOnTrack(trackExplorer.getPosition());
        int x = tempP.getX();
        int y = tempP.getY();
        ImPoint targetPoint = stopsHandler.arrivesAtPoint(x, y);

        int currentPosition = tempP.getOpposite().toInt();
        ReadOnlyWorld world = trackExplorer.getWorld();
        PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(world,
                targetPoint);
        int[] targets = new int[t.length];

        for (int i = 0; i < t.length; i++) {
            int target = t[i].getOpposite().toInt();

            if (target == currentPosition) {
                stopsHandler.updateTarget();
            }

            targets[i] = target;
        }

        FlatTrackExplorer tempExplorer;
        try {
            tempExplorer = new FlatTrackExplorer(world, tempP);
        } catch (NoTrackException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        int next = pathFinder.findstep(currentPosition, targets, tempExplorer);

        if (next == IncrementalPathFinder.PATH_NOT_FOUND) {
            trackExplorer.nextEdge();
            trackExplorer.moveForward();

            return trackExplorer.getVertexConnectedByEdge();
        }
        tempP.setValuesFromInt(next);
        tempP = tempP.getOpposite();

        int nextPosition = tempP.toInt();
        trackExplorer.setPosition(nextPosition);

        mr.process(stopsHandler.getMoves());
        return nextPosition;
    }
}