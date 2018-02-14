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

/*
 *
 */
package freerails.server.gamemodel;

import freerails.move.Move;
import freerails.move.TimeTickMove;
import freerails.move.mapupdatemove.WorldDiffMove;
import freerails.move.WorldDiffMoveCause;
import freerails.network.movereceiver.MoveReceiver;
import freerails.server.*;
import freerails.server.automaton.ServerAutomaton;
import freerails.server.automaton.TrainUpdater;
import freerails.world.world.FullWorldDiffs;
import freerails.world.ITEM;
import freerails.world.world.World;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameSpeed;
import freerails.world.game.GameTime;

import java.util.ArrayList;
import java.util.List;

/**
 * A ServerGameModel that contains the automations used in the actual game.
 */
public class FullServerGameModel implements ServerGameModel {

    private static final long serialVersionUID = 3978144352788820021L;
    /**
     * List of the ServerAutomaton objects connected to this game.
     */
    private final List<ServerAutomaton> serverAutomata = new ArrayList<>();
    private World world;
    private transient SupplyAtStationsUpdater supplyAtStationsUpdater;
    private TrainUpdater trainUpdater;
    private String[] passwords;
    /**
     * Number of ticks since the last time we did an infrequent update.
     */
    private int ticksSinceUpdate = 0;
    private transient long nextModelUpdateDue;
    private transient MoveReceiver moveReceiver;

    /**
     *
     */
    public FullServerGameModel() {
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     *
     */
    public synchronized void update() {
        long frameStartTime = System.currentTimeMillis();

        while (nextModelUpdateDue <= frameStartTime) {
            // First do the things that need doing whether or not the game is paused.

            trainUpdater.buildTrains(world);

            int gameSpeed = ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();

            if (gameSpeed > 0) {
                // update the time first, since other updates might need to know the current time.
                moveReceiver.process(TimeTickMove.getMove(world));

                // now do the other updates like moving the trains
                trainUpdater.moveTrains(world);

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

                // calculate "ideal world" time for next tick
                nextModelUpdateDue = nextModelUpdateDue + (1000 / gameSpeed);

                ticksSinceUpdate++;
            } else {
                nextModelUpdateDue = System.currentTimeMillis();
            }
        }
    }

    /**
     * @param moveReceiver
     */
    public void initialize(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
        trainUpdater = new TrainUpdater(moveReceiver);
        supplyAtStationsUpdater = new SupplyAtStationsUpdater(world, moveReceiver);

        for (ServerAutomaton serverAutomaton : serverAutomata) {
            serverAutomaton.initAutomaton(moveReceiver);
        }

        trainUpdater.initAutomaton(moveReceiver);
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * @param world
     * @param passwords
     */
    public void setWorld(World world, String[] passwords) {
        this.world = world;
        this.passwords = passwords.clone();
        serverAutomata.clear();
    }

    /**
     * @return
     */
    public String[] getPasswords() {
        return passwords.clone();
    }

    /**
     * This is called on the last tick of each year.
     */
    private void yearEnd() {
        TrackMaintenanceMoveGenerator trackMaintenanceMoveGenerator = new TrackMaintenanceMoveGenerator(moveReceiver);
        trackMaintenanceMoveGenerator.update(world);

        TrainMaintenanceMoveGenerator trainMaintenanceMoveGenerator = new TrainMaintenanceMoveGenerator(moveReceiver);
        trainMaintenanceMoveGenerator.update(world);

        BondInterestMoveGenerator bondInterestMoveGenerator = new BondInterestMoveGenerator(moveReceiver);
        bondInterestMoveGenerator.update(world);

        // Grow cities.
        FullWorldDiffs fullWorldDiffs = new FullWorldDiffs(world);
        CityTilePositioner cityTilePositioner = new CityTilePositioner(fullWorldDiffs);
        cityTilePositioner.growCities();

        Move move = new WorldDiffMove(world, fullWorldDiffs, WorldDiffMoveCause.YearEnd);
        moveReceiver.process(move);
    }

    /**
     * This is called at the start of each new month.
     */
    private void monthEnd() {
        supplyAtStationsUpdater.update();

        CargoAtStationsUpdater cargoAtStationsUpdater = new CargoAtStationsUpdater();
        cargoAtStationsUpdater.update(world, moveReceiver);
    }

}