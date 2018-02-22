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
package freerails.server;

import freerails.move.Move;
import freerails.move.TimeTickMove;
import freerails.move.mapupdatemove.WorldDiffMove;
import freerails.move.WorldDiffMoveCause;
import freerails.move.receiver.MoveReceiver;
import freerails.model.world.FullWorldDiffs;
import freerails.model.world.WorldItem;
import freerails.model.world.World;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;

/**
 * A ServerGameModel that contains the automations used in the actual game. This is serialized during loading and
 * saving of games.
 */
public class FullServerGameModel implements ServerGameModel {

    private static final long serialVersionUID = 3978144352788820021L;
    private World world;
    private TrainUpdater trainUpdater;
    private String[] passwords;

    private transient SupplyAtStationsUpdater supplyAtStationsUpdater;
    private transient long nextModelUpdateDue;
    private transient MoveReceiver moveReceiver;

    /**
     *
     */
    public FullServerGameModel() {
        nextModelUpdateDue = System.currentTimeMillis();
    }

    /**
     * Update of some kind.
     */
    public synchronized void update() {
        long frameStartTime = System.currentTimeMillis();

        while (nextModelUpdateDue <= frameStartTime) {
            // First do the things that need doing whether or not the game is paused.

            trainUpdater.buildTrains(world);

            int gameSpeed = ((GameSpeed) world.get(WorldItem.GameSpeed)).getSpeed();

            if (gameSpeed > 0) {
                // update the time first, since other updates might need to know the current time.
                moveReceiver.process(TimeTickMove.generate(world));

                // now do the other updates like moving the trains
                trainUpdater.moveTrains(world);

                // Check whether we are about to start a new year..
                GameTime time = world.currentTime();
                GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
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