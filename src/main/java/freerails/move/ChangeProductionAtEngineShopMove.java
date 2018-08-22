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


import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.train.TrainTemplate;

import java.util.List;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 */
public class ChangeProductionAtEngineShopMove implements Move {

    private static final long serialVersionUID = 3905519384997737520L;
    private final List<TrainTemplate> before;
    private final List<TrainTemplate> after;
    private final int stationNumber;
    private final Player player;

    /**
     * @param b
     * @param a
     * @param station
     * @param player
     */
    public ChangeProductionAtEngineShopMove(List<TrainTemplate> b, List<TrainTemplate> a, int station, Player player) {
        before = b;
        after = a;
        stationNumber = station;
        this.player = player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeProductionAtEngineShopMove)) return false;

        final ChangeProductionAtEngineShopMove changeProductionAtEngineShopMove = (ChangeProductionAtEngineShopMove) obj;

        if (stationNumber != changeProductionAtEngineShopMove.stationNumber) return false;
        if (after != null ? !after.equals(changeProductionAtEngineShopMove.after) : changeProductionAtEngineShopMove.after != null)
            return false;
        if (before != null ? !before.equals(changeProductionAtEngineShopMove.before) : changeProductionAtEngineShopMove.before != null)
            return false;
        return player.equals(changeProductionAtEngineShopMove.player);
    }

    @Override
    public int hashCode() {
        int result;
        result = (before != null ? before.hashCode() : 0);
        result = 29 * result + (after != null ? after.hashCode() : 0);
        result = 29 * result + stationNumber;
        result = 29 * result + player.hashCode();
        return result;
    }

    @Override
    public Status applicable(UnmodifiableWorld world) {
        // Check that the specified station exists.
        // TODO check that station is existing, do we need a dedicated function for that
        /*
        if (!world.boundsContain(player, PlayerKey.Stations, stationNumber)) {
            return MoveStatus.moveFailed(stationNumber + " " + player);
        } */

        Station station = null;
        try {
            station = world.getStation(player, stationNumber);
        } catch (Exception e) {}

        if (null == station) {
            return Status.fail(stationNumber + " " + player + " is does null");
        }

        // Check that the station is building what we expect.
        if (null == station.getProduction()) {
            if (null == before) {
                return Status.OK;
            }
            return Status.fail(stationNumber + " " + player);
        }
        if (station.getProduction().equals(before)) {
            return Status.OK;
        }
        return Status.fail(stationNumber + " " + player);
    }

    @Override
    public void apply(World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        Station station = world.getStation(player, stationNumber);
        station.setProduction(after);
    }
}