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

package org.railz.server;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.railz.config.LogManager;
import org.railz.controller.MoveChainFork;
import org.railz.controller.ServerCommand;
import org.railz.controller.ServerCommandReceiver;
import org.railz.controller.SourcedMoveReceiver;
import org.railz.move.ChangeCalendarMove;
import org.railz.move.ChangeProductionAtEngineShopMove;
import org.railz.move.TimeTickMove;
import org.railz.util.FreerailsProgressMonitor;
import org.railz.util.GameModel;
import org.railz.world.common.GameCalendar;
import org.railz.world.common.GameTime;
import org.railz.world.common.VictoryConditions;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.ProductionAtEngineShop;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.WorldImpl;
import org.railz.world.train.TrainModel;

/**
 * This class takes care of the world simulation - for instance "non-player"
 * activities.
 * 
 * @author Luke Lindsay 05-Nov-2002
 * 
 */
public class ServerGameEngine implements GameModel, Runnable, ServerCommandReceiver {
    
    private static final String CLASS_NAME = ServerGameEngine.class.getName();
    private static final Logger logger = LogManager.getLogger(CLASS_NAME);
    /**
     * Objects that run as part of the server should use this object as the
     * destination for moves, rather than queuedMoveReceiver
     */
    private final AuthoritativeMoveExecuter moveExecuter;
    private final QueuedMoveReceiver queuedMoveReceiver;
    private ServerCommandReceiver serverCommandReceiver;
    private WorldImpl world;
    private Scenario scenario;
    private ScenarioManager scenarioManager;
    private ScriptingEngine scriptingEngine;
    // private static final Logger logger = Logger.getLogger("global");
    
    /* some stats for monitoring sim speed */
    private int statUpdates = 0;
    private long statLastTimestamp = 0;
    private final MoveChainFork moveChainFork;
    private CalcSupplyAtStations calcSupplyAtStations;
    TrainBuilder tb;
    private int targetTicksPerSecond = 0;
    private IdentityProvider identityProvider;
    private TaxationMoveFactory taxationMoveFactory;
    private BalanceSheetMoveFactory balanceSheetMoveFactory;
    private AccountInterestMoveFactory accountInterestMoveFactory;
    private TrainMaintenanceMoveFactory trainMaintenanceMoveFactory;
    private StatGatherer statsGatherer;
    
    /**
     * List of the ServerAutomaton objects connected to this game
     */
    private Vector serverAutomata;
    
    private long frameStartTime;
    private long nextModelUpdateDue = System.currentTimeMillis();
    private long baseTime = System.currentTimeMillis();
    
    /**
     * number of ticks since baseTime
     */
    private int n;
    AuthoritativeTrainMover trainMover;
    TrainController trainController;
    private int currentYearLastTick;
    private int currentMonthLastTick;
    private boolean keepRunning = true;
    
    /**
     * Start a game on a new instance of a named map
     */
    public ServerGameEngine(String mapName, FreerailsProgressMonitor pm, Scenario scenario) {
	this(WorldFactory.createWorldFromMapFile(mapName, pm), new Vector(), scenario);
    }
    
    /**
     * Starts a game with the specified world state
     * 
     * @param serverAutomata
     *            Vector of ServerAutomaton representing internal clients of
     *            this game.
     * @param scenario
     *            a scenario which this game is going to have as an objective.
     * @param w
     *            a {@link org.railz.world.top.WorldImpl} containing the world
     *            state.
     */
    private ServerGameEngine(WorldImpl w, Vector serverAutomata, Scenario scenario) {
	this.world = w;
	this.serverAutomata = serverAutomata;
	this.scenario = scenario;
	
	scenarioManager = new ScenarioManager(world, scenario, this);
	VictoryConditions vc = new VictoryConditions(scenario.getName(), scenario.getDescription());
	world.set(ITEM.VICTORY_CONDITIONS, vc, Player.AUTHORITATIVE);
	
	moveChainFork = new MoveChainFork();
	
	moveExecuter = new AuthoritativeMoveExecuter(world, moveChainFork);
	statsGatherer = new StatGatherer(w, moveExecuter);
	identityProvider = new IdentityProvider(this, scenario, statsGatherer);
	queuedMoveReceiver = new QueuedMoveReceiver(moveExecuter, identityProvider);
	tb = new TrainBuilder(world, moveExecuter);
	calcSupplyAtStations = new CalcSupplyAtStations(w, moveExecuter);
	moveChainFork.addListListener(calcSupplyAtStations);
	taxationMoveFactory = new TaxationMoveFactory(w, moveExecuter);
	balanceSheetMoveFactory = new BalanceSheetMoveFactory(w, moveExecuter);
	accountInterestMoveFactory = new AccountInterestMoveFactory(w, moveExecuter);
	trainMaintenanceMoveFactory = new TrainMaintenanceMoveFactory(w, moveExecuter);
	trainMover = new AuthoritativeTrainMover(w, moveExecuter);
	trainController = new TrainController(w, moveExecuter);
	scriptingEngine = new ScriptingEngine(w, moveExecuter);
	
	for (int i = 0; i < serverAutomata.size(); i++) {
	    ((ServerAutomaton) serverAutomata.get(i)).initAutomaton(moveExecuter);
	}
	
	nextModelUpdateDue = System.currentTimeMillis();
	
	GameTime t = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	t = new GameTime(t.getTime() - 1);
	GameCalendar gc = (GameCalendar) w.get(ITEM.CALENDAR, Player.AUTHORITATIVE);
	currentYearLastTick = gc.getCalendar(t).get(Calendar.YEAR);
	currentMonthLastTick = gc.getCalendar(t).get(Calendar.MONTH);
	
	/* Start the server thread */
	Thread thread = new Thread(this);
	thread.start();
    }
    
    public int getTargetTicksPerSecond() {
	return targetTicksPerSecond;
    }
    
    public synchronized void setTargetTicksPerSecond(int targetTicksPerSecond) {
	if (this.targetTicksPerSecond == 0) {
	    baseTime = System.currentTimeMillis();
	} else {
	    baseTime = frameStartTime;
	}
	this.targetTicksPerSecond = targetTicksPerSecond;
	n = 0;
	GameCalendar oldCal = (GameCalendar) world.get(ITEM.CALENDAR, Player.AUTHORITATIVE);
	GameCalendar newCal = new GameCalendar(oldCal, targetTicksPerSecond);
	moveExecuter.processMove(new ChangeCalendarMove(oldCal, newCal));
    }
    
    /**
     * Method to keep the server running and call the update method.
     */
    public void run() {
	final String METHOD_NAME = "run";
	logger.log(Level.INFO, "Railz server thread started.");
	Thread.currentThread().setName("Railz server");
	
	/*
	 * bump this threads priority so we always gain control.
	 */
	Thread.currentThread().setPriority(Thread.currentThread().getPriority() + 1);
	
	try {
	    while (keepRunning) {
		// logger.logp(Level.FINER, CLASS_NAME, METHOD_NAME,
		// "Entering new tick.");
		try {
		    update();
		} catch (Exception ex) {
		    
		    logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
			    "Error: Unhandled exception in date ticker!!", ex);
		}
	    }
	} catch (Throwable t) {
	    logger.log(Level.SEVERE, "Caught throwable " + t, t);
	    logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
		    "Error: Unhandled throwable in date ticker!!", t);
	    return;
	}
    }
    
    /**
     * Exit the game loop
     */
    public void stop() {
	keepRunning = false;
    }
    
    /**
     * This is the main server update method, which does all the "simulation".
     * <p>
     * Each tick scheduled to start at baseTime + 1000 * n / fps
     * 
     * <p>
     * <b>Overview of Scheduling strategy</b>
     * <p>
     * <b>Goal of strategy</b>
     * <p>
     * The goal of the scheduling is to achieve the desired number of ticks
     * (frames) per second or as many as possible if this is not achievable and
     * provide the maximum possible remaining time to clients.
     * <p>
     * <b>Methodology</b>
     * <p>
     * This method allows for a maximum "jitter" of +1 <i>client</i> frame
     * interval. (assuming we are the highest priority thread competing when the
     * client relinquishes control).
     * <ol>
     * <li>Server thread enters update loop for frame n.
     * <li>The server thread performs the required updates to the game model.
     * <li>Server calculates the desired time at which frame n+1 should start
     * using t_(n+1) = t_0 + n * frame_interval. t_0 is the time at which frame
     * 0 was scheduled.
     * <li>Server wakes up at some time not earlier than t_(n+1).
     * <li>repeat.
     * </ol>
     */
    public synchronized void update() {
	if (targetTicksPerSecond > 0) {
	    queuedMoveReceiver.executeOutstandingMoves();
	    
	    // start of server world update
	    updateGameTime();
	    
	    // now do the other updates
	    moveTrains();
	    buildTrains();
	    
	    triggerMonthlyOrAnuallyTasks();
	    statUpdate();
	    
	    /* calculate "ideal world" time for next tick */
	    nextModelUpdateDue = baseTime + (1000 * n) / targetTicksPerSecond;
	    int delay = (int) (nextModelUpdateDue - frameStartTime);
	    
	    /*
	     * wake up any waiting client threads - we could be more agressive,
	     * and only notify them if delay > 0?
	     */
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
		// When the game is frozen we don't want to be spinning in a
		// loop.
		Thread.sleep(200);
	    } catch (InterruptedException e) {
		// do nothing
	    }
	}
    }
    
    /**
     * Schedule next tick
     */
    private void statUpdate() {
	/*
	 * all world updates done... now schedule next tick
	 */
	statUpdates++;
	n++;
	frameStartTime = System.currentTimeMillis();
	
	if (statUpdates == 100) {
	    /*
	     * every 100 ticks, calculate some stats and reset the base time
	     */
	    statUpdates = 0;
	    
	    int updatesPerSec = (int) (100000L / (frameStartTime - statLastTimestamp));
	    
	    if (statLastTimestamp > 0) {
		logger.log(Level.FINER, "Updates per sec " + updatesPerSec);
	    }
	    
	    statLastTimestamp = frameStartTime;
	    
	    baseTime = frameStartTime;
	    n = 0;
	}
	
    }
    
    /**
     * Method to trigger monthly or annual tasks.
     * 
     * These include... track maintenance costs.
     */
    private void triggerMonthlyOrAnuallyTasks() {
	// Check whether we have just started a new year..
	GameTime time = (GameTime) world.get(ITEM.TIME);
	GameCalendar calendar = (GameCalendar) world.get(ITEM.CALENDAR);
	int currentYear = calendar.getCalendar(time).get(Calendar.YEAR);
	int currentMonth = calendar.getCalendar(time).get(Calendar.MONTH);
	
	if (this.currentMonthLastTick != currentMonth) {
	    this.currentMonthLastTick = currentMonth;
	    newMonth();
	}
	if (this.currentYearLastTick != currentYear) {
	    this.currentYearLastTick = currentYear;
	    newYear(currentYear - 1);
	}
	
    }
    
    /**
     * Trigger monthly tasks.
     * 
     * This include Track Maintenance costs. Account interest. Cargo at
     * stations. Scripting engine?
     * 
     */
    private void newMonth() {
	calcSupplyAtStations.doProcessing();
	TrackMaintenanceMoveGenerator tmmg = new TrackMaintenanceMoveGenerator(moveExecuter);
	tmmg.update(world);
	accountInterestMoveFactory.generateMoves();
	trainMaintenanceMoveFactory.generateMoves();
	
	CargoAtStationsGenerator cargoAtStationsGenerator = new CargoAtStationsGenerator(
		moveExecuter);
	cargoAtStationsGenerator.update(world);
	scriptingEngine.processScripts();
    }
    
    /**
     * This is called at the start of each new year.
     * 
     * Tax calculations, balance sheet move, stats gathering and scenario
     * manager (check victory).
     * 
     * @param lastYear
     *            the year which has just elapsed
     */
    private void newYear(int lastYear) {
	taxationMoveFactory.generateMoves(lastYear);
	balanceSheetMoveFactory.generateMoves();
	statsGatherer.generateMoves();
	scenarioManager.checkVictory();
    }
    
    /**
     * Iterate over the stations and build trains at any that have their
     * production field set.
     */
    private void buildTrains() {
	NonNullElements j = new NonNullElements(KEY.PLAYERS, world);
	while (j.next()) {
	    FreerailsPrincipal principal = ((Player) j.getElement()).getPrincipal();
	    for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
		StationModel station = (StationModel) world.get(KEY.STATIONS, i, principal);
		
		if (null != station && null != station.getProduction()) {
		    ProductionAtEngineShop production = station.getProduction();
		    Point p = new Point(station.x, station.y);
		    
		    tb.buildTrain(production.getEngineType(), production.getWagonTypes(), p,
			    principal);
		    
		    moveExecuter.processMove(new ChangeProductionAtEngineShopMove(production, null,
			    i, principal));
		}
	    }
	}
    }
    
    private void moveTrains() {
	trainMover.moveTrains();
	trainController.updateTrains();
    }
    
    /**
     * update the time first, since other updates might need to know the current
     * time.
     */
    private void updateGameTime() {
	moveExecuter.processMove(TimeTickMove.getMove(world));
    }
    
    public synchronized void saveGame(File filename) {
	// World
	// filename
	
	try {
	    logger.log(Level.INFO, "Saving game..  ");
	    NonNullElements i = new NonNullElements(KEY.PLAYERS, world, Player.AUTHORITATIVE);
	    GameTime t = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	    while (i.next()) {
		NonNullElements j = new NonNullElements(KEY.TRAINS, world,
			((Player) i.getElement()).getPrincipal());
		while (j.next()) {
		    TrainModel tm = ((TrainModel) j.getElement());
		    trainMover.releaseAllLocks(world, tm.getPosition(t), tm);
		}
	    }
	    
	    FileOutputStream out = new FileOutputStream(filename.getCanonicalPath());
	    GZIPOutputStream zipout = new GZIPOutputStream(out);
	    
	    ObjectOutputStream objectOut = new ObjectOutputStream(zipout);
	    
	    objectOut.writeObject(world);
	    objectOut.writeObject(serverAutomata);
	    objectOut.writeObject(scenario);
	    
	    /**
	     * save player private data
	     */
	    i = new NonNullElements(KEY.PLAYERS, world, Player.AUTHORITATIVE);
	    
	    while (i.next()) {
		((Player) i.getElement()).saveSession(objectOut);
	    }
	    
	    objectOut.flush();
	    objectOut.close();
	    
	    logger.log(Level.INFO, "done.");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    
    /**
     * load a game from a saved position
     */
    public static ServerGameEngine loadGame(File filename) throws IOException {
	ServerGameEngine engine = null;
	
	try {
	    System.out.print("Loading game..  ");
	    
	    FileInputStream in = new FileInputStream(filename.getCanonicalPath());
	    GZIPInputStream zipin = new GZIPInputStream(in);
	    ObjectInputStream objectIn = new ObjectInputStream(zipin);
	    
	    WorldImpl world = (WorldImpl) objectIn.readObject();
	    Vector serverAutomata = (Vector) objectIn.readObject();
	    Scenario scenario = (Scenario) objectIn.readObject();
	    
	    /**
	     * load player private data
	     */
	    NonNullElements i = new NonNullElements(KEY.PLAYERS, world, Player.AUTHORITATIVE);
	    
	    while (i.next()) {
		((Player) i.getElement()).loadSession(objectIn);
	    }
	    
	    engine = new ServerGameEngine(world, serverAutomata, scenario);
	    
	    /*
	     * i = new NonNullElements(KEY.PLAYERS, world,
	     * Player.AUTHORITATIVE); while (i.next()) { FreerailsPrincipal p =
	     * ((Player) i.getElement()).getPrincipal(); NonNullElements j = new
	     * NonNullElements(KEY.TRAINS, world, p); while (j.next()) {
	     * TrainModel tm = (TrainModel) j.getElement(); assert
	     * !tm.isBlocked(); } }
	     */

	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new IOException(ex.getMessage());
	}
	
	return engine;
    }
    
    /**
     * Returns a reference to the servers world.
     * 
     * @return World
     */
    public WorldImpl getWorld() {
	return world;
    }
    
    /**
     * @return Returns a moveReceiver - moves are submitted from clients to the
     *         ServerGameEngine via this.
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
    
    public void setServerCommandReceiver(ServerCommandReceiver r) {
	serverCommandReceiver = r;
    }
    
    public void sendCommand(ServerCommand s) {
	if (serverCommandReceiver != null)
	    serverCommandReceiver.sendCommand(s);
    }
}
