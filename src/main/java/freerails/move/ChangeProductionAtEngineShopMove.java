/*
 * Created on 28-Mar-2003
 *
 */
package freerails.move;

import freerails.world.common.ImList;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.PlannedTrain;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.World;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 *
 * @author Luke
 */
public class ChangeProductionAtEngineShopMove implements Move {
    private static final long serialVersionUID = 3905519384997737520L;

    private final ImList<PlannedTrain> before;

    private final ImList<PlannedTrain> after;

    private final int stationNumber;

    private final FreerailsPrincipal principal;

    /**
     *
     * @param b
     * @param a
     * @param station
     * @param p
     */
    public ChangeProductionAtEngineShopMove(ImList<PlannedTrain> b,
                                            ImList<PlannedTrain> a, int station, FreerailsPrincipal p) {
        this.before = b;
        this.after = a;
        this.stationNumber = station;
        this.principal = p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeProductionAtEngineShopMove))
            return false;

        final ChangeProductionAtEngineShopMove changeProductionAtEngineShopMove = (ChangeProductionAtEngineShopMove) o;

        if (stationNumber != changeProductionAtEngineShopMove.stationNumber)
            return false;
        if (after != null ? !after
                .equals(changeProductionAtEngineShopMove.after)
                : changeProductionAtEngineShopMove.after != null)
            return false;
        if (before != null ? !before
                .equals(changeProductionAtEngineShopMove.before)
                : changeProductionAtEngineShopMove.before != null)
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

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, before);
    }

    private MoveStatus tryMove(World w, ImList<PlannedTrain> stateA) {
        // Check that the specified station exists.
        if (!w.boundsContain(principal, KEY.STATIONS, this.stationNumber)) {
            return MoveStatus.moveFailed(this.stationNumber + " " + principal);
        }

        StationModel station = (StationModel) w.get(principal, KEY.STATIONS,
                stationNumber);

        if (null == station) {
            return MoveStatus.moveFailed(this.stationNumber + " " + principal
                    + " is does null");
        }

        // Check that the station is building what we expect.
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            }
            return MoveStatus.moveFailed(this.stationNumber + " " + principal);
        }
        if (station.getProduction().equals(stateA)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed(this.stationNumber + " " + principal);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel) w.get(principal,
                    KEY.STATIONS, stationNumber);
            station = new StationModel(station, this.after);
            w.set(principal, KEY.STATIONS, stationNumber, station);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel) w.get(principal,
                    KEY.STATIONS, stationNumber);
            station = new StationModel(station, this.before);
            w.set(principal, KEY.STATIONS, stationNumber, station);
        }

        return status;
    }

}