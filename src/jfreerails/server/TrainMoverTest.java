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

import java.awt.Point;
import java.util.ArrayList;

import jfreerails.controller.ToAndFroPathIterator;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.world.common.*;
import jfreerails.world.top.*;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.player.Player;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainMoverTest extends TestCase {
    TrainMover trainMover;
    World w;
    ArrayList points;  

    private Player testPlayer = new Player ("test player", (new Player ("test"
		    + " player")).getPublicKey(), 0);

    /**
     * Constructor for TrainMoverTest.
     * @param arg0
     */
    public TrainMoverTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
	w = new WorldImpl(0, 0);
	points = new ArrayList();
	GameTime now = new GameTime(0);
	w.set(ITEM.TIME, now, Player.AUTHORITATIVE);

	points.add(new Point(0, 0));
	points.add(new Point(80, 80));
	points.add(new Point(150, 100));

	w.add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);

	TrainModel train = new TrainModel(0, new int[0], null, 0, 0, now);

	w.add(KEY.TRAINS, train, testPlayer.getPrincipal());

	if (null == w.get(KEY.TRAINS, 0, testPlayer.getPrincipal())) {
	    throw new NullPointerException();
	}

	FreerailsPathIterator to = new ToAndFroPathIterator(points);
	FreerailsPathIterator from = new ToAndFroPathIterator(points);
	trainMover = new TrainMover(to, w, 0, testPlayer.getPrincipal());

	Move move = trainMover.setInitialTrainPosition(train, from);
	MoveStatus ms = move.doMove(w, Player.AUTHORITATIVE);

	if (!ms.isOk()) {
	    throw new IllegalStateException(ms.message);
	}
    }

    public void testTrainMover() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());

        TrainPositionOnMap pos = t.getPosition();

        assertEquals(pos.getX(0), 0);
        assertEquals(pos.getY(0), 0);

        PathWalker pw = trainMover.getWalker();

        pw.stepForward(10);

        IntLine line = new IntLine();

        pw.nextSegment(line);

        assertEquals(line.x1, 0);
        assertEquals(line.y1, 0);
    }

    public void testUpdate() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());

        TrainPositionOnMap pos = t.getPosition();

        ChangeTrainPositionMove m = trainMover.update(30);

        m.doMove(w, testPlayer.getPrincipal());
    }
}
