/*
 * Copyright (C) 2002 Luke Lindsay
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

import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.move.InitialiseTrainPositionMove;
import jfreerails.move.Move;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPathIterator;
import jfreerails.world.train.TrainPositionOnMap;


/**
 * Responsible for moving the trains.
 *
 * @author Luke Lindsay 27-Oct-2002
 *
 */
class TrainMover implements FreerailsServerSerializable {
    final PathWalker walker;
    final int trainNumber;
    final ReadOnlyWorld w;
    final TrainPathFinder trainPathFinder;
    final FreerailsPrincipal trainPrincipal;

    private TrainMover(TrainPathFinder pathFinder, FreerailsPathIterator to,
	    ReadOnlyWorld world, int trainNo, FreerailsPrincipal tp) {
        this.trainNumber = trainNo;
        this.w = world;
        walker = new PathWalkerImpl(to);
        trainPathFinder = pathFinder;
	trainPrincipal = tp;
    }
    
    public TrainMover(FreerailsPathIterator to, ReadOnlyWorld world, int
	    trainNo, FreerailsPrincipal tp) {
	this(null, to, world, trainNo, tp);
    }

    public TrainMover(TrainPathFinder pathFinder, ReadOnlyWorld world,
        int trainNo, FreerailsPrincipal tp) {
	this(pathFinder, new TrainPathIterator(pathFinder), world, trainNo, tp);
    }

    Move setInitialTrainPosition(TrainModel train,
        FreerailsPathIterator from) {
        int trainLength = train.getLength();
        PathWalker fromPathWalker = new PathWalkerImpl(from);
        fromPathWalker.stepForward(trainLength);

	TrainPositionOnMap initialPosition =
	    TrainPositionOnMap.createInSameDirectionAsPath(fromPathWalker);

        return new InitialiseTrainPositionMove(trainNumber, trainPrincipal,
	       	initialPosition);
    }

    public PathWalker getWalker() {
        return walker;
    }

    public ChangeTrainPositionMove update(int distanceTravelled) {
        double distanceTravelledAsDouble = distanceTravelled;
        double distance = distanceTravelledAsDouble * getTrainSpeed();
        walker.stepForward(distance);

	return ChangeTrainPositionMove.generate(w, walker, trainNumber,
		trainPrincipal);
    }

    public double getTrainSpeed() {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber,
		trainPrincipal);
        int trainLength = train.getNumberOfWagons();

        //For now train speeds are hard coded.
        switch (trainLength) {
        case 0:
            return 1;

        case 1:
            return 0.8;

        case 2:
            return 0.6;

        case 3:
            return 0.4;

        case 4:
            return 0.3;

        default:
            return 0.2;
        }
    }

    public TrainPathFinder getTrainPathFinder() {
        return trainPathFinder;
    }
}
