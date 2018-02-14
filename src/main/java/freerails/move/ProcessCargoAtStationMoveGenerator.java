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
package freerails.move;

import freerails.world.KEY;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoBatchBundle;
import freerails.world.finances.CargoDeliveryMoneyTransaction;
import freerails.world.finances.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Generates Moves that pay the player for delivering the cargo.
 */
public class ProcessCargoAtStationMoveGenerator {

    /**
     * Determines how much the player gets for delivering cargo. Changed from
     * 100 to 75 to fix bug 910132 (Too easy to make money!)
     */
    private static final int MAGIC_NUMBER = 75;

    private ProcessCargoAtStationMoveGenerator() {
    }

    /**
     * @param w
     * @param bundle
     * @param stationID
     * @param p
     * @param trainId
     * @return
     */
    public static ArrayList<Move> processCargo(ReadOnlyWorld w, CargoBatchBundle bundle, int stationID, FreerailsPrincipal p, int trainId) {
        Station thisStation = (Station) w.get(p, KEY.STATIONS, stationID);
        Iterator<CargoBatch> batches = bundle.cargoBatchIterator();

        ArrayList<Move> moves = new ArrayList<>();

        while (batches.hasNext()) {
            CargoBatch batch = batches.next();
            // TODO own function for that
            double distanceSquared = (batch.getSourceP().x - thisStation.location.x) * (batch.getSourceP().x - thisStation.location.x) + (batch.getSourceP().y - thisStation.location.y) * (batch.getSourceP().y - thisStation.location.y);
            double dist = Math.sqrt(distanceSquared);
            int quantity = bundle.getAmount(batch);

            double amount = quantity * Math.log(dist) * MAGIC_NUMBER;
            Money money = new Money((long) amount);
            CargoDeliveryMoneyTransaction receipt = new CargoDeliveryMoneyTransaction(money, quantity, stationID, batch, trainId);
            moves.add(new AddTransactionMove(p, receipt));
        }

        return moves;
    }
}