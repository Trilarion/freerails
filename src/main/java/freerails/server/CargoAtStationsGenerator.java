/*
 * Created on 31-May-2003
 *
 */
package freerails.server;

import freerails.controller.FreerailsServerSerializable;
import freerails.move.ChangeCargoBundleMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.station.SupplyAtStation;
import freerails.world.top.*;

import java.util.Iterator;

/**
 * This class loops over the list of stations and adds cargo depending on what
 * the surrounding tiles supply.
 *
 * @author Luke
 */
public class CargoAtStationsGenerator implements FreerailsServerSerializable {
    private static final long serialVersionUID = 3834596504072959796L;

    public CargoAtStationsGenerator() {
    }

    /**
     * Call this method once a month.
     */
    public void update(World w, MoveReceiver moveReceiver) {
        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            NonNullElements nonNullStations = new NonNullElements(KEY.STATIONS,
                    w, principal);

            while (nonNullStations.next()) {
                StationModel station = (StationModel) nonNullStations
                        .getElement();
                SupplyAtStation supply = station.getSupply();
                ImmutableCargoBundle cargoBundle = (ImmutableCargoBundle) w
                        .get(principal, KEY.CARGO_BUNDLES, station
                                .getCargoBundleID());
                MutableCargoBundle before = new MutableCargoBundle(cargoBundle);
                MutableCargoBundle after = new MutableCargoBundle(cargoBundle);
                int stationNumber = nonNullStations.getIndex();

                /*
                 * Get the iterator from a copy to avoid a
                 * ConcurrentModificationException if the amount gets set to
                 * zero and the CargoBatch removed from the cargo bundle. LL
                 */
                Iterator<CargoBatch> it = after.toImmutableCargoBundle()
                        .cargoBatchIterator();

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
                        CargoBatch cb = new CargoBatch(i, station.x, station.y,
                                0, stationNumber);
                        int amountAlready = after.getAmount(cb);

                        // Obtain the month
                        GameTime time = w.currentTime();
                        GameCalendar calendar = (GameCalendar) w
                                .get(ITEM.CALENDAR);
                        int month = calendar.getMonth(time.getTicks());

                        int amountAfter = calculateAmountToAdd(amountSupplied,
                                month)
                                + amountAlready;
                        after.setAmount(cb, amountAfter);
                    }
                }

                Move m = new ChangeCargoBundleMove(before
                        .toImmutableCargoBundle(), after
                        .toImmutableCargoBundle(), station.getCargoBundleID(),
                        principal);
                moveReceiver.processMove(m);
            }
        }
    }

    int calculateAmountToAdd(int amountSuppliedPerYear, int month) {
        // Note, jan is month 0.
        int totalAtMonthEnd = amountSuppliedPerYear * (month + 1) / 12;
        int totalAtMonthStart = amountSuppliedPerYear * (month) / 12;
        int amount = totalAtMonthEnd - totalAtMonthStart;

        return amount;
    }
}