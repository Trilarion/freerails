/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.server;

import java.awt.Point;
import java.util.Vector;

import jfreerails.controller.MoveReceiver;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.controller.pathfinder.SimpleAStarPathFinder;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.server.ServerAutomaton;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ObjectKey;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;


/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints.
 *
 *
 * @author Luke Lindsay
 * 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {
    public static final int NOT_AT_STATION = -1;
    private final int trainId;
    private final ReadOnlyWorld world;
    private transient MoveReceiver moveReceiver;
    private final FreerailsPrincipal trainPrincipal;
    FlatTrackExplorer trackExplorer;
    SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();

    /**
     * Constructor.
     *
     * @param tx the track explorer this pathfinder is to use.
     * @param tp Principal that owns the train.
     */
    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
        int trainNumber, FreerailsPrincipal tp, MoveReceiver mr) {
        this.moveReceiver = mr;
        this.trackExplorer = tx;
        this.trainId = trainNumber;
        this.world = w;
	trainPrincipal = tp;
    }

    public boolean hasNextInt() {
        return trackExplorer.hasNextEdge();
    }

    /**
     * @return a move that initialises the trains schedule.
     */
    public Move initTarget(TrainModel train, ImmutableSchedule currentSchedule) {
        Vector moves = new Vector();
        int scheduleID = train.getScheduleID();
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;
        ObjectKey stationNumber = schedule.getStationToGoto();
	station = (StationModel)world.get(KEY.STATIONS, stationNumber.index,
		stationNumber.principal);

        int[] wagonsToAdd = schedule.getWagonsToAdd();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            moves.add(ChangeTrainMove.generateMove(this.trainId, trainPrincipal,
		       	train, engine, wagonsToAdd));
        }

        schedule.gotoNextStaton();

        ImmutableSchedule newSchedule = schedule.toImmutableSchedule();

        ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                currentSchedule, newSchedule);
        moves.add(move);

        return new CompositeMove((Move[])moves.toArray(new Move[1]));
    }

    /**
     * Issues a ChangeTrainScheduleMove to set the train to move to the next
     * station.
     */
    private void updateTarget() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
		trainPrincipal);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule currentSchedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID);
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;
        scheduledStop();
        schedule.gotoNextStaton();

        ImmutableSchedule newSchedule = schedule.toImmutableSchedule();

        ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                currentSchedule, newSchedule);
        moveReceiver.processMove(move);

        ObjectKey stationNumber = schedule.getStationToGoto();
	station = (StationModel)world.get(KEY.STATIONS, stationNumber.index,
		stationNumber.principal);

        if (null == station) {
            System.err.println("null == station, train " + trainId +
                " doesn't know where to go next!");
        }
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     */
    private Point getTarget() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
		trainPrincipal);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID);
        ObjectKey stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber.index) {
            //There are no stations on the schedule.
            return new Point(0, 0);
        }

        StationModel station = (StationModel)world.get(KEY.STATIONS,
                stationNumber.index, stationNumber.principal);

        return new Point(station.x, station.y);
    }

    private void scheduledStop() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
		trainPrincipal);
        Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                train.getScheduleID());
        StationModel station = null;
        ObjectKey stationNumber = schedule.getStationToGoto();
	station = (StationModel)world.get(KEY.STATIONS, stationNumber.index,
		stationNumber.principal);

        int[] wagonsToAdd = schedule.getWagonsToAdd();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
	    Move m = ChangeTrainMove.generateMove(this.trainId, trainPrincipal,
		    train, engine, wagonsToAdd);
            moveReceiver.processMove(m);
        }
    }

    private void loadAndUnloadCargo(ObjectKey stationId) {
        //train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new   
	    DropOffAndPickupCargoMoveGenerator(trainPrincipal, trainId,
                stationId.principal, stationId.index, world);

        Move m = transfer.generateMove();
        moveReceiver.processMove(m);
    }

    /**
     * @return the number of the station the train is currently at, or -1 if
     * no current station.
     */
    public ObjectKey getStationNumber(int x, int y) {
	//loop thru the station list to check if train is at the same Point as
	//a station
	NonNullElements j = new NonNullElements(KEY.PLAYERS, world);
	while (j.next()) {
	    FreerailsPrincipal p = ((Player)
		    j.getElement()).getPrincipal();
	    for (int i = 0; i < world.size(KEY.STATIONS, p); i++) {
		StationModel tempPoint = (StationModel)world.get(KEY.STATIONS,
			i, p);

		if (null != tempPoint && (x == tempPoint.x) && (y ==
			    tempPoint.y)) {
		    //train is at the station at location tempPoint
		    return new ObjectKey(KEY.STATIONS, p, i);
		}
	    }
	}

        return null;
        //there are no stations that exist where the train is currently
    }

    /**
     * @return a PositionOnTrack packed into an int
     */
    public int nextInt() {
        PositionOnTrack tempP = new PositionOnTrack(trackExplorer.getPosition());
        Point targetPoint = getTarget();

        if (tempP.getX() == targetPoint.x && tempP.getY() == targetPoint.y) {
            //One of the things updateTarget() does is change the train consist, so
            //it should be called before loadAndUnloadCargo(stationNumber)
            updateTarget();
            targetPoint = getTarget();
        }

        ObjectKey stationKey = getStationNumber(tempP.getX(), tempP.getY());

        if (stationKey != null) {
            loadAndUnloadCargo(stationKey);
        }

        int currentPosition = tempP.getOpposite().toInt();

        PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(trackExplorer.getWorld(),
                new Point(targetPoint.x, targetPoint.y));
        int[] targets = new int[t.length];

        for (int i = 0; i < t.length; i++) {
            int target = t[i].getOpposite().toInt();

            if (target == currentPosition) {
                updateTarget();
            }

            targets[i] = target;
        }

        FlatTrackExplorer tempExplorer = new FlatTrackExplorer(trackExplorer.getWorld(),
                tempP);
        int next = pathFinder.findpath(currentPosition, targets, tempExplorer);

        if (next == SimpleAStarPathFinder.PATH_NOT_FOUND) {
            trackExplorer.nextEdge();
            trackExplorer.moveForward();

            return trackExplorer.getVertexConnectedByEdge();
        } else {
            tempP.setValuesFromInt(next);
            tempP = tempP.getOpposite();

            int nextPosition = tempP.toInt();
            trackExplorer.setPosition(nextPosition);

            return nextPosition;
        }
    }

    public void initAutomaton(MoveReceiver mr) {
        moveReceiver = mr;
    }
}
