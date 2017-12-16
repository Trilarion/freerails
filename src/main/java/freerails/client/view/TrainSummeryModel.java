/**
 * FreeRails 2 - A railroad strategy game Copyright (C) 2007 Roland Spatzenegger
 * (c@npg.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 
 */
package freerails.client.view;

import java.util.HashMap;
import java.util.Map;

import freerails.client.view.TrainOrdersListModel.TrainOrdersListElement;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.train.TrainOrdersModel;

public class TrainSummeryModel {

    private static final long MINIMUM_WAIT_TIME = 250;
    private ReadOnlyWorld world = null;
    private int lastNrOfTransactions = 0;
    private Map<Integer, Money> lastTrainIncome;
    private FreerailsPrincipal principal = null;
    private int maxTrainNum = 0;

    private long lastUpdate;
    private Map<Integer, String> lastStations;
    private long lastStationUpdate;

    public TrainSummeryModel() {
        lastTrainIncome = new HashMap<Integer, Money>(64);
        lastUpdate = lastStationUpdate = System.currentTimeMillis();
        lastStations = new HashMap<Integer, String>(64);
    }

    public void setWorld(ReadOnlyWorld world, FreerailsPrincipal principal) {
        if (this.world != world) {
            this.world = world;
        }
        if (this.principal != principal) {
            this.principal = principal;
        }
    }

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
