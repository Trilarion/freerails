/*
 * Created on Sep 10, 2004
 *
 */
package freerails.server;

import freerails.move.TimeTickMove;
import freerails.move.WorldDiffMove;
import freerails.network.MoveReceiver;
import freerails.network.ServerGameModel;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameSpeed;
import freerails.world.common.GameTime;
import freerails.world.player.Player;
import freerails.world.top.ITEM;
import freerails.world.top.World;
import freerails.world.top.WorldDiffs;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * A ServerGameModel that contains the automations used in the actual game.
 *
 * @author Luke
 */
public class ServerGameModelImpl implements ServerGameModel {
    private static final long serialVersionUID = 3978144352788820021L;
    /**
     * List of the ServerAutomaton objects connected to this game.
     */
    private final Vector<ServerAutomaton> serverAutomata;

    /**
     *
     */
    public World world;
    private transient CalcSupplyAtStations calcSupplyAtStations;
    private TrainUpdater tb;
    private String[] passwords;
    /**
     * Number of ticks since the last time we did an infrequent update.
     */
    private int ticksSinceUpdate = 0;

    private transient long nextModelUpdateDue;

    private transient MoveReceiver moveExecuter;

    /**
     *
     */
    public ServerGameModelImpl() {
        this(null, new Vector<>());
    }

    /**
     *
     * @param w
     * @param serverAutomata
     */
    public ServerGameModelImpl(World w, Vector<ServerAutomaton> serverAutomata) {
        this.world = w;
        this.serverAutomata = serverAutomata;

        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * This is called on the last tick of each year.
     */
    private void yearEnd() {
        TrackMaintenanceMoveGenerator tmmg = new TrackMaintenanceMoveGenerator(
                moveExecuter);
        tmmg.update(world);

        TrainMaintenanceMoveGenerator trainMaintenanceMoveGenerator = new TrainMaintenanceMoveGenerator(
                moveExecuter);
        trainMaintenanceMoveGenerator.update(world);

        InterestChargeMoveGenerator interestChargeMoveGenerator = new InterestChargeMoveGenerator(
                moveExecuter);
        interestChargeMoveGenerator.update(world);

        // Grow cities.
        WorldDiffs wd = new WorldDiffs(world);
        CityTilePositioner ctp = new CityTilePositioner(wd);
        ctp.growCities();

        WorldDiffMove move = new WorldDiffMove(world, wd,
                WorldDiffMove.Cause.YearEnd);
        moveExecuter.processMove(move);
    }

    /**
     * This is called at the start of each new month.
     */
    private void monthEnd() {
        calcSupplyAtStations.doProcessing();

        CargoAtStationsGenerator cargoAtStationsGenerator = new CargoAtStationsGenerator();
        cargoAtStationsGenerator.update(world, moveExecuter);
    }

    private void updateGameTime() {
        moveExecuter.processMove(TimeTickMove.getMove(world));
    }

    /**
     *
     */
    public synchronized void update() {
        long frameStartTime = System.currentTimeMillis();

        while (nextModelUpdateDue <= frameStartTime) {
            /*
             * First do the things that need doing whether or not the game is
             * paused.
             */
            tb.buildTrains(world);

            int gameSpeed = ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();

            if (gameSpeed > 0) {
                /*
                 * Update the time first, since other updates might need to know
                 * the current time.
                 */
                updateGameTime();

                // now do the other updates
                tb.moveTrains(world);

                // Check whether we are about to start a new year..
                GameTime time = world.currentTime();
                GameCalendar calendar = (GameCalendar) world.get(ITEM.CALENDAR);
                int yearNextTick = calendar.getYear(time.getTicks() + 1);
                int yearThisTick = calendar.getYear(time.getTicks());

                if (yearThisTick != yearNextTick) {
                    yearEnd();
                }

                // And a new month..
                int monthThisTick = calendar.getMonth(time.getTicks());
                int monthNextTick = calendar.getMonth(time.getTicks() + 1);

                if (monthNextTick != monthThisTick) {
                    monthEnd();
                }

                /* calculate "ideal world" time for next tick */
                nextModelUpdateDue = nextModelUpdateDue + (1000 / gameSpeed);

                // int delay = (int)(nextModelUpdateDue - frameStartTime);
                //
                // /* wake up any waiting client threads - we could be
                // * more agressive, and only notify them if delay > 0? */
                // this.notifyAll();
                //
                // try {
                // if (delay > 0) {
                // this.wait(delay);
                // } else {
                // this.wait(1);
                // }
                // } catch (InterruptedException e) {
                // // do nothing
                // }
                ticksSinceUpdate++;
            } else {
                // try {
                // //When the game is frozen we don't want to be spinning in a
                // //loop.
                // Thread.sleep(200);
                // } catch (InterruptedException e) {
                // // do nothing
                // }
                nextModelUpdateDue = System.currentTimeMillis();
            }
        }
    }

    /**
     *
     * @param objectOut
     * @throws IOException
     */
    public void write(ObjectOutputStream objectOut) throws IOException {

        objectOut.writeObject(world);
        objectOut.writeObject(serverAutomata);

        /*
          save player private data
         */
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            Player player = world.getPlayer(i);
            player.saveSession(objectOut);
        }
    }

    /**
     *
     * @param newMoveExecuter
     */
    public void init(MoveReceiver newMoveExecuter) {
        this.moveExecuter = newMoveExecuter;
        tb = new TrainUpdater(newMoveExecuter);
        calcSupplyAtStations = new CalcSupplyAtStations(world, newMoveExecuter);

        for (ServerAutomaton aServerAutomata : serverAutomata) {
            aServerAutomata.initAutomaton(newMoveExecuter);
        }

        tb.initAutomaton(newMoveExecuter);
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     *
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     *
     * @param w
     * @param passwords
     */
    public void setWorld(World w, String[] passwords) {
        this.world = w;
        this.serverAutomata.clear();
        this.passwords = passwords.clone();
    }

    /**
     *
     * @return
     */
    public String[] getPasswords() {
        return passwords.clone();
    }
}