/*
  FreeRails 2 - A railroad strategy game Copyright (C) 2007 Roland Spatzenegger
  (c@npg.net)
  <p>
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.
  <p>
  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.
  <p>
  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 51
  Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package freerails.client.view;

import freerails.client.view.TrainOrdersListModel.TrainOrdersListElement;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.train.TrainOrdersModel;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jkeller1
 */
public class TrainSummeryModel {

    private static final long MINIMUM_WAIT_TIME = 250;
    private final Map<Integer, Money> lastTrainIncome;
    private final Map<Integer, String> lastStations;
    private ReadOnlyWorld world = null;
    private int lastNrOfTransactions = 0;
    private FreerailsPrincipal principal = null;
    private int maxTrainNum = 0;
    private long lastUpdate;
    private long lastStationUpdate;

    /**
     *
     */
    public TrainSummeryModel() {
        lastTrainIncome = new HashMap<>(64);
        lastUpdate = lastStationUpdate = System.currentTimeMillis();
        lastStations = new HashMap<>(64);
    }

    /**
     *
     * @param world
     * @param principal
     */
    public void setWorld(ReadOnlyWorld world, FreerailsPrincipal principal) {
        if (this.world != world) {
            this.world = world;
        }
        if (this.principal != principal) {
            this.principal = principal;
        }
    }

    /**
     *
     * @param trainNum
     * @return
     */
    public Money findTrainIncome(int trainNum) {
        int numberOfTransactions = world.getNumberOfTransactions(principal);
        long currentTime = System.currentTimeMillis();
        if (lastUpdate + MINIMUM_WAIT_TIME > currentTime
                || numberOfTransactions == lastNrOfTransactions) {
            // not necessary ...
            Money m = lastTrainIncome.get(trainNum);
            if (m != null) {
                return m;
            }
            // but we don't have it
        } else {
            lastNrOfTransactions = numberOfTransactions;
            lastUpdate = currentTime;
        }
        IncomeStatementGenerator income = new IncomeStatementGenerator(world,
                principal);
        maxTrainNum = Math.max(maxTrainNum, trainNum);
        Money[] m = new Money[maxTrainNum + 1];
        income.calTrainRevenue(m);
        for (int i = 0; i < maxTrainNum; i++) {
            lastTrainIncome.put(i, m[i]);
        }
        return m[trainNum];
    }

    /**
     *
     * @param trainNum
     * @return
     */
    public String getStationName(int trainNum) {
        long currentTime = System.currentTimeMillis();
        if (lastStationUpdate + MINIMUM_WAIT_TIME > currentTime) {
            String lastStation = lastStations.get(trainNum);
            if (lastStation != null) {
                return lastStation;
            }
        }
        lastStationUpdate = currentTime;
        TrainOrdersModel orders = null;
        TrainOrdersListModel ordersList = new TrainOrdersListModel(world,
                trainNum, principal);
        int size = ordersList.getSize();
        for (int i = 0; i < size; ++i) {
            TrainOrdersListElement element = (TrainOrdersListElement) ordersList
                    .getElementAt(i);
            if (element.gotoStatus == TrainOrdersListModel.GOTO_NOW) {
                orders = element.order;
                break;
            }
        }
        StationModel station = (StationModel) world.get(principal,
                KEY.STATIONS, orders.getStationID());
        String stationName = station.getStationName();
        lastStations.put(trainNum, stationName);
        return stationName;

    }
}
