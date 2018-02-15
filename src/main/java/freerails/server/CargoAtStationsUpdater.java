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

import freerails.model.world.WorldItem;
import freerails.model.world.SharedKey;
import freerails.model.world.PlayerKey;
import freerails.move.listmove.ChangeCargoBundleMove;
import freerails.move.Move;
import freerails.network.movereceiver.MoveReceiver;
import freerails.model.*;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.cargo.MutableCargoBatchBundle;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.station.StationSupply;
import freerails.model.world.World;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Loops over the list of stations and adds cargo depending on what
 * the surrounding tiles supply.
 */
public class CargoAtStationsUpdater implements Serializable {

    private static final long serialVersionUID = 3834596504072959796L;

    /**
     *
     */
    public CargoAtStationsUpdater() {}

    /**
     * Call this method once a month.
     */
    public void update(World world, MoveReceiver moveReceiver) {

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
    public int calculateAmountToAddPerMonth(int amountSuppliedPerYear, int month) {
        // This calculation actually delivers the requirement of rounding sometimes up and sometimes down.
        return amountSuppliedPerYear * (month + 1) / 12 - amountSuppliedPerYear * (month) / 12;
    }
}