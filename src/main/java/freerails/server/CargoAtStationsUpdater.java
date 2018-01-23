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

import freerails.move.ChangeCargoBundleMove;
import freerails.move.Move;
import freerails.world.*;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.ImmutableCargoBatchBundle;
import freerails.world.cargo.MutableCargoBatchBundle;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.station.StationSupply;

import java.util.Iterator;

/**
 * Loops over the list of stations and adds cargo depending on what
 * the surrounding tiles supply.
 */
class CargoAtStationsUpdater implements FreerailsServerSerializable {

    private static final long serialVersionUID = 3834596504072959796L;

    /**
     *
     */
    public CargoAtStationsUpdater() {
    }

    /**
     * Call this method once a month.
     */
    public void update(World w, MoveReceiver moveReceiver) {
        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            NonNullElementWorldIterator nonNullStations = new NonNullElementWorldIterator(KEY.STATIONS, w, principal);

            while (nonNullStations.next()) {
                Station station = (Station) nonNullStations.getElement();
                StationSupply supply = station.getSupply();
                ImmutableCargoBatchBundle cargoBundle = (ImmutableCargoBatchBundle) w.get(principal, KEY.CARGO_BUNDLES, station.getCargoBundleID());
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

                for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
                    int amountSupplied = supply.getSupply(i);

                    if (amountSupplied > 0) {
                        CargoBatch cb = new CargoBatch(i, station.p, 0, stationNumber);
                        int amountAlready = after.getAmount(cb);

                        // Obtain the month
                        GameTime time = w.currentTime();
                        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
                        int month = calendar.getMonth(time.getTicks());

                        int amountAfter = calculateAmountToAdd(amountSupplied, month) + amountAlready;
                        after.setAmount(cb, amountAfter);
                    }
                }

                Move move = new ChangeCargoBundleMove(before.toImmutableCargoBundle(), after.toImmutableCargoBundle(), station.getCargoBundleID(), principal);
                moveReceiver.process(move);
            }
        }
    }

    int calculateAmountToAdd(int amountSuppliedPerYear, int month) {
        // Note, jan is month 0.
        int totalAtMonthEnd = amountSuppliedPerYear * (month + 1) / 12;
        int totalAtMonthStart = amountSuppliedPerYear * (month) / 12;

        return totalAtMonthEnd - totalAtMonthStart;
    }
}