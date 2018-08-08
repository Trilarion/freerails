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


import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.station.TrainBlueprint;

import java.util.List;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 */
public class ChangeProductionAtEngineShopMove implements Move {

    private static final long serialVersionUID = 3905519384997737520L;
    private final List<TrainBlueprint> before;
    private final List<TrainBlueprint> after;
    private final int stationNumber;
    private final Player player;

    /**
     * @param b
     * @param a
     * @param station
     * @param player
     */
    public ChangeProductionAtEngineShopMove(List<TrainBlueprint> b, List<TrainBlueprint> a, int station, Player player) {
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

    public MoveStatus tryDoMove(World world, Player player) {
        return tryMove(world, before);
    }

    private MoveStatus tryMove(World world, List<TrainBlueprint> stateA) {
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
            return MoveStatus.moveFailed(stationNumber + " " + player + " is does null");
        }

        // Check that the station is building what we expect.
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            }
            return MoveStatus.moveFailed(stationNumber + " " + player);
        }
        if (station.getProduction().equals(stateA)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed(stationNumber + " " + player);
    }

    public MoveStatus tryUndoMove(World world, Player player) {
        return tryMove(world, after);
    }

    public MoveStatus doMove(World world, Player player) {
        MoveStatus status = tryDoMove(world, player);

        if (status.succeeds()) {
            Station station = world.getStation(this.player, stationNumber);
            station.setProduction(after);
        }
        return status;
    }

    public MoveStatus undoMove(World world, Player player) {
        MoveStatus status = tryUndoMove(world, player);

        if (status.succeeds()) {
            Station station = world.getStation(this.player, stationNumber);
            station.setProduction(before);
        }
        return status;
    }
}