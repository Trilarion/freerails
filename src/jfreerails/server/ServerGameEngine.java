package jfreerails.server;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jfreerails.controller.MoveChainFork;
import jfreerails.controller.SourcedMoveReceiver;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.move.TimeTickMove;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.util.GameModel;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;


/**
 *
 * This class takes care of the world simulation - for instance "non-player" activities.
 * @author Luke Lindsay 05-Nov-2002
 *
 */
public class ServerGameEngine implements GameModel, Runnable {
    /**
     * Objects that run as part of the server should use this object as the
     * destination for moves, rather than queuedMoveReceiver
     */
    private final AuthoritativeMoveExecuter moveExecuter;
    private final QueuedMoveReceiver queuedMoveReceiver;
    private World world;

    /* some stats for monitoring sim speed */
    private int statUpdates = 0;
    private long statLastTimestamp = 0;
    private final MoveChainFork moveChainFork;
    private CalcSupplyAtStations calcSupplyAtStations;
    TrainBuilder tb;
    private int targetTicksPerSecond = 0;
    private IdentityProvider identityProvider;

    /**
     * List of the ServerAutomaton objects connected to this game
     */
    private Vector serverAutomata;

    /**
     * Number of ticks which is A Long Time for infrequently updated things.
     * TODO Ideally we should calculate this from the calendar
     */
    private final static int aLongTime = 1000;

    /**
     * Number of ticks since the last time we did an infrequent update
     */
    private int ticksSinceUpdate = 0;
    private long frameStartTime;
    private long nextModelUpdateDue = System.currentTimeMillis();
    private long baseTime = System.currentTimeMillis();

    /**
     * number of ticks since baseTime
     */
    private int n;
    ArrayList trainMovers = new ArrayList();
    private int currentYearLastTick = -1;
    private int currentMonthLastTick = -1;
    private boolean keepRunning = true;

    public int getTargetTicksPerSecond() {
        return targetTicksPerSecond;
    }

    public synchronized void setTargetTicksPerSecond(int targetTicksPerSecond) {
        this.targetTicksPerSecond = targetTicksPerSecond;
    }

    /**
     * Start a game on a new instance of a named map
     */
    public ServerGameEngine(String mapName, FreerailsProgressMonitor pm) {
        this(new ArrayList(), OldWorldImpl.createWorldFromMapFile(mapName, pm),
            new Vector());
    }

    /**
     * Starts a game with the specified world state
     * @param trainMovers ArrayList of TrainMover objects.
     * @param serverAutomata Vector of ServerAutomaton representing internal
     * clients of this game.
     * @param p an IdentityProvider which correlates a ConnectionToServer
     * object with a Principal.
     */
    private ServerGameEngine(ArrayList trainMovers, World w,
        Vector serverAutomata) {
        this.world = w;
        this.serverAutomata = serverAutomata;
        this.trainMovers = trainMovers;

        moveChainFork = new MoveChainFork();

        moveExecuter = new AuthoritativeMoveExecuter(world, moveChainFork);
        identityProvider = new IdentityProvider(this);
        queuedMoveReceiver = new QueuedMoveReceiver(moveExecuter,
                identityProvider);
        tb = new TrainBuilder(world, moveExecuter);
        calcSupplyAtStations = new CalcSupplyAtStations(w, moveExecuter);
        moveChainFork.addListListener(calcSupplyAtStations);

        for (int i = 0; i < serverAutomata.size(); i++) {
            ((ServerAutomaton)serverAutomata.get(i)).initAutomaton(moveExecuter);
        }

        nextModelUpdateDue = System.currentTimeMillis();

        /* Start the server thread */
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        Thread.currentThread().setName("Railz server");

        /*
         * bump this threads priority so we always gain control.
        */
        Thread.currentThread().setPriority(Thread.currentThread().getPriority() +
            1);

        while (keepRunning) {
            update();
        }
    }

    /**
     * Exit the game loop
     */
    public void stop() {
        keepRunning = false;
    }

    public void infrequentUpdate() {
        calcSupplyAtStations.doProcessing();
    }

    /**
     * This is the main server update method, which does all the
     * "simulation".
     * <p>Each tick scheduled to start at baseTime + 1000 * n / fps
     *
     * <p><b>Overview of Scheduling strategy</b>
     * <p><b>Goal of strategy</b>
     * <p> The goal of the scheduling is to achieve the desired number of
     * ticks (frames) per second or as many as possible if this is not
     * achievable and provide the maximum possible remaining time to
     * clients.
     * <p><b>Methodology</b>
     * <p>This method allows for a maximum "jitter" of +1 <i>client</i>
     * frame interval. (assuming we are the highest priority thread
     * competing when the client relinquishes control).
     * <ol>
     * <li>Server thread enters update loop for frame n.
     * <li>The server thread performs the required updates to the game
     * model.
     * <li>Server calculates the desired time at which frame n+1 should
     * start using t_(n+1) = t_0 + n * frame_interval. t_0 is the time at which
     * frame 0 was scheduled.
     * <li>Server wakes up at some time not earlier than t_(n+1).
     * <li>repeat.
     * </ol>
     */
    public synchronized void update() {
        if (targetTicksPerSecond > 0) {
            queuedMoveReceiver.executeOutstandingMoves();

            /*
             * start of server world update
             */
            //update the time first, since other updates might need
            //to know the current time.
            updateGameTime();

            //now do the other updates
            moveTrains();

            /*  Note, an Exception gets thrown if moveTrains() is called after buildTrains()
             * without first calling moveExecuter.executeOutstandingMoves()
             */
            buildTrains();

            //Check whether we have just started a new year..
            GameTime time = (GameTime)world.get(ITEM.TIME);
            GameCalendar calendar = (GameCalendar)world.get(ITEM.CALENDAR);
            int currentYear = calendar.getCalendar(time).get(Calendar.YEAR);
	    int currentMonth = calendar.getCalendar(time).get(Calendar.MONTH);

	    if (this.currentMonthLastTick != currentMonth) {
		this.currentMonthLastTick = currentMonth;
		newMonth();
	    }
            if (this.currentYearLastTick != currentYear) {
                this.currentYearLastTick = currentYear;
                newYear();
            }

            if (ticksSinceUpdate % aLongTime == 0) {
                infrequentUpdate();
            }

            /*
             * all world updates done... now schedule next tick
             */
            statUpdates++;
            n++;
            frameStartTime = System.currentTimeMillis();

            if (statUpdates == 100) {
                /* every 100 ticks, calculate some stats and reset
                 * the base time */
                statUpdates = 0;

                int updatesPerSec = (int)(100000L / (frameStartTime -
                    statLastTimestamp));

                if (statLastTimestamp > 0) {
                    //	System.out.println(
                    //		"Updates per sec " + updatesPerSec);
                }

                statLastTimestamp = frameStartTime;

                baseTime = frameStartTime;
                n = 0;
            }

            /* calculate "ideal world" time for next tick */
            nextModelUpdateDue = baseTime + (1000 * n) / targetTicksPerSecond;

            int delay = (int)(nextModelUpdateDue - frameStartTime);

            /* wake up any waiting client threads - we could be
             * more agressive, and only notify them if delay > 0? */
            this.notifyAll();

            try {
                if (delay > 0) {
                    this.wait(delay);
                } else {
                    this.wait(1);
                }
            } catch (InterruptedException e) {
                // do nothing
            }

            ticksSinceUpdate++;
        } else {
            /*
             * even when game is paused, we should still check for moves
             * submitted by players due to execution of ServerCommands on the
             * server
             */
            queuedMoveReceiver.executeOutstandingMoves();
            // desired tick rate was 0
            nextModelUpdateDue = frameStartTime;

            try {
                //When the game is frozen we don't want to be spinning in a
                //loop.
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private void newMonth() {
        TrackMaintenanceMoveGenerator tmmg = new TrackMaintenanceMoveGenerator(moveExecuter);
        tmmg.update(world);

        CargoAtStationsGenerator cargoAtStationsGenerator = new CargoAtStationsGenerator(moveExecuter);
        cargoAtStationsGenerator.update(world);
    }

    /** This is called at the start of each new year. */
    private void newYear() {
    }

    /**
     * Iterate over the stations
     * and build trains at any that have their production
     * field set.
     */
    private void buildTrains() {
	NonNullElements j = new NonNullElements(KEY.PLAYERS, world);
	while (j.next()) {
	    FreerailsPrincipal principal = ((Player)
		    j.getElement()).getPrincipal();
	    for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
		StationModel station = (StationModel)world.get(KEY.STATIONS, i,
			principal);

		if (null != station && null != station.getProduction()) {
		    ProductionAtEngineShop production = station.getProduction();
		    Point p = new Point(station.x, station.y);
		    
		    TrainMover trainMover = tb.buildTrain
			(production.getEngineType(),
			 production.getWagonTypes(), p, principal);

		    //FIXME, at some stage 'ServerAutomaton' and 'trainMovers' should be combined.
		    TrainPathFinder tpf = trainMover.getTrainPathFinder();
		    this.addServerAutomaton(tpf);
		    this.addTrainMover(trainMover);
		    moveExecuter.processMove(new ChangeProductionAtEngineShopMove(
				production, null, i, principal));
		}
	    }
	}
    }

    private void moveTrains() {
        int deltaDistance = 5;

        ChangeTrainPositionMove m = null;

        Iterator i = trainMovers.iterator();

        while (i.hasNext()) {
            Object o = i.next();
            TrainMover trainMover = (TrainMover)o;
            m = trainMover.update(deltaDistance);
            moveExecuter.processMove(m);
        }
    }

    private void updateGameTime() {
        moveExecuter.processMove(TimeTickMove.getMove(world));
    }

    public void addTrainMover(TrainMover m) {
        trainMovers.add(m);
    }

    public synchronized void saveGame(File filename) {
        try {
            System.out.print("Saving game..  ");

            FileOutputStream out = new
		FileOutputStream(filename.getCanonicalPath());
            GZIPOutputStream zipout = new GZIPOutputStream(out);

            ObjectOutputStream objectOut = new ObjectOutputStream(zipout);

            objectOut.writeObject(trainMovers);
            objectOut.writeObject(world);
            objectOut.writeObject(serverAutomata);

            /**
             * save player private data
             */
            NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
                    Player.AUTHORITATIVE);

            while (i.next()) {
                ((Player)i.getElement()).saveSession(objectOut);
            }

            objectOut.flush();
            objectOut.close();

            System.out.println("done.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * load a game from a saved position
     */
    public static ServerGameEngine loadGame(File filename) {
        ServerGameEngine engine = null;

        try {
            System.out.print("Loading game..  ");

            FileInputStream in = new
		FileInputStream(filename.getCanonicalPath());
            GZIPInputStream zipin = new GZIPInputStream(in);
            ObjectInputStream objectIn = new ObjectInputStream(zipin);
            ArrayList trainMovers = (ArrayList)objectIn.readObject();
            World world = (World)objectIn.readObject();
            Vector serverAutomata = (Vector)objectIn.readObject();

            /**
             * load player private data
             */
            NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
                    Player.AUTHORITATIVE);

            while (i.next()) {
                ((Player)i.getElement()).loadSession(objectIn);
            }

            engine = new ServerGameEngine(trainMovers, world, serverAutomata);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return engine;
    }

    /**
     * Returns a reference to the servers world.
     * @return World
     */
    public synchronized World getWorld() {
        return world;
    }

    /**
     * @return Returns a moveReceiver - moves are submitted from clients to the
     * ServerGameEngine via this.
     */
    public SourcedMoveReceiver getMoveExecuter() {
        return queuedMoveReceiver;
    }

    /**
     * @return The MoveChainFork to which clients of this server may attach
     */
    public MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    public void addServerAutomaton(ServerAutomaton sa) {
        serverAutomata.add(sa);
    }

    public void removeServerAutomaton(ServerAutomaton sa) {
        serverAutomata.remove(sa);
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }
}
