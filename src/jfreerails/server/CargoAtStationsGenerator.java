/*
 * Created on 31-May-2003
 *
 */
package jfreerails.server;

import java.util.Iterator;
import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;


/**
 * This class loops over the list of stations and adds cargo
 * depending on what the surrounding tiles supply.
 *
 * @author Luke
 *
 */
public class CargoAtStationsGenerator implements FreerailsServerSerializable {
    public CargoAtStationsGenerator() {
    }

    /** Call this method once a month.*/
    public void update(World w, MoveReceiver moveReceiver) {
        for (int k = 0; k < w.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = w.getPlayer(k).getPrincipal();

            NonNullElements nonNullStations = new NonNullElements(KEY.STATIONS,
                    w, principal);

            while (nonNullStations.next()) {
                StationModel station = (StationModel)nonNullStations.getElement();
                SupplyAtStation supply = station.getSupply();
                CargoBundle cargoBundle = (CargoBundle)w.get(KEY.CARGO_BUNDLES,
                        station.getCargoBundleNumber(), principal);
                CargoBundle before = cargoBundle.getCopy();
                CargoBundle after = cargoBundle.getCopy();
                int stationNumber = nonNullStations.getIndex();

                /* Get the iterator from a copy to avoid a
                 * ConcurrentModificationException if the amount
                 * gets set to zero and the CargoBatch removed from
                 * the cargo bundle. LL
                 */
                Iterator it = after.getCopy().cargoBatchIterator();

                while (it.hasNext()) {
                    CargoBatch cb = (CargoBatch)it.next();
                    int amount = after.getAmount(cb);

                    if (amount > 0) {
                        //(23/24)^12 = 0.60 
                        after.setAmount(cb, amount * 23 / 24);
                    }
                }

                for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
                    int amountSupplied = supply.getSupply(i);

                    if (amountSupplied > 0) {
                        CargoBatch cb = new CargoBatch(i, station.x, station.y,
                                0, stationNumber);
                        int amountAlready = after.getAmount(cb);

                        //Obtain the month
                        GameTime time = (GameTime)w.get(ITEM.TIME);
                        GameCalendar calendar = (GameCalendar)w.get(ITEM.CALENDAR);
                        int month = calendar.getMonth(time.getTime());

                        int amountAfter = calculateAmountToAdd(amountSupplied,
                                month) + amountAlready;
                        after.setAmount(cb, amountAfter);
                    }
                }

                Move m = new ChangeCargoBundleMove(before, after,
                        station.getCargoBundleNumber(), principal);
                moveReceiver.processMove(m);
            }
        }
    }

    int calculateAmountToAdd(int amountSuppliedPerYear, int month) {
        //Note, jan is month 0.
        int totalAtMonthEnd = amountSuppliedPerYear * (month + 1) / 12;
        int totalAtMonthStart = amountSuppliedPerYear * (month) / 12;
        int amount = totalAtMonthEnd - totalAtMonthStart;

        return amount;
    }
}