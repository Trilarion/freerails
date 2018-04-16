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

import freerails.model.NonNullElementWorldIterator;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.cargo.MutableCargoBatchBundle;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.CalculateCargoSupplyRateAtStation;
import freerails.model.station.Station;
import freerails.model.station.StationSupply;
import freerails.model.world.*;
import freerails.move.GrowCitiesMove;
import freerails.move.Move;
import freerails.move.TimeTickMove;
import freerails.move.generator.BondInterestMoveGenerator;
import freerails.move.listmove.ChangeCargoBundleMove;
import freerails.move.listmove.ChangeStationMove;
import freerails.move.receiver.MoveReceiver;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;

import java.util.Iterator;

/**
 * A ServerGameModel that contains the automations used in the actual game. This is serialized during loading and
 * saving of games.
 */
public class FullServerGameModel implements ServerGameModel {

    private static final long serialVersionUID = 3978144352788820021L;
    private World world;
    private TrainUpdater trainUpdater;
    private String[] passwords;

    private transient long nextModelUpdateDue;
    private transient MoveReceiver moveReceiver;

    /**
     *
     */
    public FullServerGameModel() {
        nextModelUpdateDue = System.currentTimeMillis();
    }

    // TODO this should be part of the model, at least the update part
    /**
     * Call this method once a month.
     *
     * Loops over the list of stations and adds cargo depending on what
     * the surrounding tiles supply.
     */
    public static void cargoAtStationsUpdate(World world, MoveReceiver moveReceiver) {

        for (int k = 0; k < world.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();

            NonNullElementWorldIterator nonNullStations = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            while (nonNullStations.next()) {
                Station station = (Station) nonNullStations.getElement();
                StationSupply supply = station.getSupply();
                ImmutableCargoBatchBundle cargoBundle = (ImmutableCargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, station.getCargoBundleID());
                MutableCargoBatchBundle before = new MutableCargoBatchBundle(cargoBundle);
                MutableCargoBatchBundle after = new MutableCargoBatchBundle(cargoBundle);
                int stationNumber = nonNullStations.getIndex();

                /*
                 * Get the iterator from a copy to avoid a
                 * ConcurrentModificationException if the amount gets set to
                 * zero and the CargoBatch removed from the cargo bundle. LL
                 */
                Iterator<CargoBatch> it = after.toImmutableCargoBundle().cargoBatchIterator();

                while (it.hasNext()) {
                    CargoBatch cb = it.next();
                    int amount = after.getAmount(cb);

                    if (amount > 0) {
                        // (23/24)^12 = 0.60
                        after.setAmount(cb, amount * 23 / 24);
                    }
                }

                for (int i = 0; i < world.size(SharedKey.CargoTypes); i++) {
                    int amountSupplied = supply.getSupply(i);

                    if (amountSupplied > 0) {
                        CargoBatch cb = new CargoBatch(i, station.location, 0, stationNumber);
                        int amountAlready = after.getAmount(cb);

                        // Obtain the month
                        GameTime time = world.currentTime();
                        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
                        int month = calendar.getMonth(time.getTicks());

                        int amountAfter = calculateAmountToAddPerMonth(amountSupplied, month) + amountAlready;
                        after.setAmount(cb, amountAfter);
                    }
                }

                Move move = new ChangeCargoBundleMove(before.toImmutableCargoBundle(), after.toImmutableCargoBundle(), station.getCargoBundleID(), principal);
                moveReceiver.process(move);
            }
        }
    }

    /**
     * If, say, 14 units get added each year, some month we should add 1 and
     * others we should add 2 such that over the year exactly 14 units get
     * added.
     *
     * Note: January is 0
     */
    public static int calculateAmountToAddPerMonth(int amountSuppliedPerYear, int month) {
        // This calculation actually delivers the requirement of rounding sometimes up and sometimes down.
        return amountSuppliedPerYear * (month + 1) / 12 - amountSuppliedPerYear * (month) / 12;
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

    // TODO make this part of the constructor
    /**
     * @param moveReceiver
     */
    public void initialize(MoveReceiver moveReceiver) {
        this.moveReceiver = moveReceiver;
        trainUpdater = new TrainUpdater(moveReceiver);
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

        // Grow cities
        Move move = new GrowCitiesMove();
        moveReceiver.process(move);
    }

    /**
     * This is called at the start of each new month.
     */
    private void monthEnd() {
        supplyAtStationsUpdate(world, moveReceiver);
        cargoAtStationsUpdate(world, moveReceiver);
    }

    /**
     * Loops through all of the known stations and recalculates the
     * cargoes that they supply, demand, and convert.
     */
    public static void supplyAtStationsUpdate(World world, MoveReceiver moveReceiver) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();
            NonNullElementWorldIterator iterator = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            while (iterator.next()) {
                Station stationBefore = (Station) iterator.getElement();
                CalculateCargoSupplyRateAtStation supplyRate;
                supplyRate = new CalculateCargoSupplyRateAtStation(world, stationBefore.location);

                Station stationAfter = supplyRate.calculations(stationBefore);

                if (!stationAfter.equals(stationBefore)) {
                    Move move = new ChangeStationMove(iterator.getIndex(), stationBefore, stationAfter, principal);
                    moveReceiver.process(move);
                }
            }
        }
    }

}