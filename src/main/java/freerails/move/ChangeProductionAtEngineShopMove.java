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

import freerails.util.ImmutableList;
import freerails.world.KEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.station.TrainBlueprint;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 */
public class ChangeProductionAtEngineShopMove implements Move {

    private static final long serialVersionUID = 3905519384997737520L;
    private final ImmutableList<TrainBlueprint> before;
    private final ImmutableList<TrainBlueprint> after;
    private final int stationNumber;
    private final FreerailsPrincipal principal;

    /**
     * @param b
     * @param a
     * @param station
     * @param p
     */
    public ChangeProductionAtEngineShopMove(ImmutableList<TrainBlueprint> b, ImmutableList<TrainBlueprint> a, int station, FreerailsPrincipal p) {
        before = b;
        after = a;
        stationNumber = station;
        principal = p;
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
        return principal.equals(changeProductionAtEngineShopMove.principal);
    }

    @Override
    public int hashCode() {
        int result;
        result = (before != null ? before.hashCode() : 0);
        result = 29 * result + (after != null ? after.hashCode() : 0);
        result = 29 * result + stationNumber;
        result = 29 * result + principal.hashCode();
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        return tryMove(world, before);
    }

    private MoveStatus tryMove(World w, ImmutableList<TrainBlueprint> stateA) {
        // Check that the specified station exists.
        if (!w.boundsContain(principal, KEY.STATIONS, stationNumber)) {
            return MoveStatus.moveFailed(stationNumber + " " + principal);
        }

        Station station = (Station) w.get(principal, KEY.STATIONS, stationNumber);

        if (null == station) {
            return MoveStatus.moveFailed(stationNumber + " " + principal + " is does null");
        }

        // Check that the station is building what we expect.
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            }
            return MoveStatus.moveFailed(stationNumber + " " + principal);
        }
        if (station.getProduction().equals(stateA)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed(stationNumber + " " + principal);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        return tryMove(world, after);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryDoMove(world, principal);

        if (status.isStatus()) {
            Station station = (Station) world.get(this.principal, KEY.STATIONS, stationNumber);
            station = new Station(station, after);
            world.set(this.principal, KEY.STATIONS, stationNumber, station);
        }
        return status;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryUndoMove(world, principal);

        if (status.isStatus()) {
            Station station = (Station) world.get(this.principal, KEY.STATIONS, stationNumber);
            station = new Station(station, before);
            world.set(this.principal, KEY.STATIONS, stationNumber, station);
        }
        return status;
    }
}