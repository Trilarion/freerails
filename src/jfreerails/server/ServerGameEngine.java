package jfreerails.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.ServerCommand;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.move.TimeTickMove;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.util.GameModel;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.World;


/**
 *
 * This class takes care of the world simulation - for instance "non-player" activities.
 * @author Luke Lindsay 05-Nov-2002
 *
 */
public class ServerGameEngine implements GameModel, Runnable {
    private static final Logger logger = Logger.getLogger(ServerGameEngine.class.getName());

    /**
    * Objects that run as part of the server should use this object as the
    * destination for moves, rather than queuedMoveReceiver.
    */
    private final AuthoritativeMoveExecuter moveExecuter;
    private final QueuedMoveReceiver queuedMoveReceiver;
    private final World world;
    private final MoveChainFork moveChainFork;
    private final CalcSupplyAtStations calcSupplyAtStations;
    private final TrainBuilder tb;
    private final IdentityProvider identityProvider;

    /**
     * List of the ServerAutomaton objects connected to this game.
     */
    private final Vector serverAutomata;

    /**
     * Number of ticks since the last time we did an infrequent update.
     */
    private int ticksSinceUpdate = 0;
    private long nextModelUpdateDue = System.currentTimeMillis();
    private int currentYearLastTick = -1;
    private int currentMonthLastTick = -1;
    private boolean keepRunning = true;

    public int getTargetTicksPerSecond() {
        return ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();
    }

    public synchronized void setTargetTicksPerSecond(int targetTicksPerSecond) {
        moveExecuter.processMove(ChangeGameSpeedMove.getMove(world,
                new GameSpeed(targetTicksPerSecond)));
    }

    /**
     * Start a game on a new instance of a named map.
     */
    public ServerGameEngine(String mapName, FreerailsProgressMonitor pm) {
        this(new ArrayList(), OldWorldImpl.createWorldFromMapFile(mapName, pm),
            new Vector());
    }

    /**
     * Starts a game with the specified world state.
     * @param trainMovers ArrayList of TrainMover objects.
     * @param serverAutomata Vector of ServerAutomaton representing internal
     * clients of this game.
     */
    private ServerGameEngine(ArrayList trainMovers, World w,
        Vector serverAutomata) {
        this.world = w;
        this.serverAutomata = serverAutomata;

        moveChainFork = new MoveChainFork();

        moveExecuter = new AuthoritativeMoveExecuter(world, moveChainFork);
        identityProvider = new IdentityProvider(this, moveExecuter);
        queuedMoveReceiver = new QueuedMoveReceiver(moveExecuter,
                identityProvider);
        tb = new TrainBuilder(moveExecuter, trainMovers);
        calcSupplyAtStations = new CalcSupplyAtStations(w, moveExecuter);
        moveChainFork.addListListener(calcSupplyAtStations);

        for (int i = 0; i < serverAutomata.size(); i++) {
            ((ServerAutomaton)serverAutomata.get(i)).initAutomaton(moveExecuter);
        }

        tb.initAutomaton(moveExecuter);

        nextModelUpdateDue = System.currentTimeMillis();

        /* Start the server thread */
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            Thread.currentThread().setName("JFreerails server");

            /*
             * bump this threads priority so we always gain control.
            */
            Thread.currentThread().setPriority(Thread.currentThread()
                                                     .getPriority() + 1);

            while (keepRunning) {
                update();
            }
        } catch (Exception e) {
            /*
            * If something goes wrong, lets kill the game straight
            * away to avoid hard-to-track-down bugs.
            */
            logger.severe("Unexpected exception, quitting..");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Exit the game loop.
     */
    public void stop() {
        keepRunning = false;
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
        long frameStartTime = System.currentTimeMillis();

        /* First do the things that need doing whether or not the game is paused.

                /*  Note, an Exception gets thrown if moveTrains() is called after buildTrains()
                * without first calling moveExecuter.executeOutstandingMoves()
                */
        tb.buildTrains(world);
        queuedMoveReceiver.executeOutstandingMoves();

        int gameSpeed = ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();

        if (gameSpeed > 0) {
            /* Update the time first, since other updates might need
            to know the current time.*/
            updateGameTime();

            //now do the other updates
            tb.moveTrains(world);

            //Check whether we have just started a new year..
            GameTime time = (GameTime)world.get(ITEM.TIME);
            GameCalendar calendar = (GameCalendar)world.get(ITEM.CALENDAR);
            int currentYear = calendar.getYear(time.getTime());

            if (this.currentYearLastTick != currentYear) {
                this.currentYearLastTick = currentYear;
                newYear();
            }

            //And then checks for a new month.,,
            int currentMonth = calendar.getMonth(time.getTime());

            if (this.currentMonthLastTick != currentMonth) {
                this.currentMonthLastTick = currentMonth;
                newMonth();
            }

            /* calculate "ideal world" time for next tick */
            nextModelUpdateDue = nextModelUpdateDue + (1000 / gameSpeed);

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
            try {
                //When the game is frozen we don't want to be spinning in a
                //loop.
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // do nothing
            }

            nextModelUpdateDue = System.currentTimeMillis();
        }
    }

    /** This is called at the start of each new year. */
    private void newYear() {
        TrackMaintenanceMoveGenerator tmmg = new TrackMaintenanceMoveGenerator(moveExecuter);
        tmmg.update(world);

        TrainMaintenanceMoveGenerator trainMaintenanceMoveGenerator = new TrainMaintenanceMoveGenerator(moveExecuter);
        trainMaintenanceMoveGenerator.update(world);

        InterestChargeMoveGenerator interestChargeMoveGenerator = new InterestChargeMoveGenerator(moveExecuter);
        interestChargeMoveGenerator.update(world);
    }

    /** This is called at the start of each new month. */
    private void newMonth() {
        calcSupplyAtStations.doProcessing();

        CargoAtStationsGenerator cargoAtStationsGenerator = new CargoAtStationsGenerator();
        cargoAtStationsGenerator.update(world, moveExecuter);
    }

    private void updateGameTime() {
        moveExecuter.processMove(TimeTickMove.getMove(world));
    }

    public synchronized void saveGame() {
        try {
            logger.info("Saving game..  ");

            FileOutputStream out = new FileOutputStream(ServerCommand.FREERAILS_SAV);
            GZIPOutputStream zipout = new GZIPOutputStream(out);

            ObjectOutputStream objectOut = new ObjectOutputStream(zipout);

            objectOut.writeObject(ServerCommand.VERSION);
            objectOut.writeObject(tb.getTrainMovers());
            objectOut.writeObject(world);
            objectOut.writeObject(serverAutomata);

            /**
             * save player private data
             */
            for (int i = 0; i < world.getNumberOfPlayers(); i++) {
                Player player = world.getPlayer(i);
                player.saveSession(objectOut);
            }

            objectOut.flush();
            objectOut.close();

            logger.fine("done.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load a game.
     */
    public static ServerGameEngine loadGame() {
        ServerGameEngine engine = null;

        try {
            logger.info("Loading game..  ");

            FileInputStream in = new FileInputStream(ServerCommand.FREERAILS_SAV);
            GZIPInputStream zipin = new GZIPInputStream(in);
            ObjectInputStream objectIn = new ObjectInputStream(zipin);
            String version_string = (String)objectIn.readObject();

            if (!ServerCommand.VERSION.equals(version_string)) {
                throw new Exception(version_string);
            }

            ArrayList trainMovers = (ArrayList)objectIn.readObject();
            World world = (World)objectIn.readObject();
            Vector serverAutomata = (Vector)objectIn.readObject();

            /**
             * load player private data
             */
            for (int i = 0; i < world.getNumberOfPlayers(); i++) {
                Player player = world.getPlayer(i);
                player.loadSession(objectIn);
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
    public MoveReceiver getMoveExecuter() {
        return queuedMoveReceiver;
    }

    /**
     * @return The MoveChainFork to which clients of this server may attach
     */
    public MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    private void addServerAutomaton(ServerAutomaton sa) {
        serverAutomata.add(sa);
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }
}