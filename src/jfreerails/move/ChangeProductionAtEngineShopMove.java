/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import java.util.Arrays;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;


/**
 * This Move changes what is being built
 * at an engine shop - when a client wants to build a train, it
 * should send an instance of this class to the server.
 *
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMove implements Move {
    private final ProductionAtEngineShop[] before;
    private final ProductionAtEngineShop[] after;
    private final int stationNumber;
    private final FreerailsPrincipal principal;

    public int hashCode() {
        int result;
        result = before.length;
        result = 29 * result + after.length;
        result = 29 * result + stationNumber;
        result = 29 * result + principal.hashCode();

        return result;
    }

    public ChangeProductionAtEngineShopMove(ProductionAtEngineShop[] b,
        ProductionAtEngineShop[] a, int station, FreerailsPrincipal p) {
        this.before = (ProductionAtEngineShop[])b.clone();
        this.after = (ProductionAtEngineShop[])a.clone();
        ;
        this.stationNumber = station;
        this.principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, before);
    }

    private MoveStatus tryMove(World w, ProductionAtEngineShop[] stateA) {
        //Check that the specified station exists.
        if (!w.boundsContain(KEY.STATIONS, this.stationNumber, principal)) {
            return MoveStatus.moveFailed(this.stationNumber + " " + principal);
        }

        StationModel station = (StationModel)w.get(KEY.STATIONS, stationNumber,
                principal);

        if (null == station) {
            return MoveStatus.moveFailed(this.stationNumber + " " + principal +
                " is does null");
        }

        //Check that the station is building what we expect.					
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.moveFailed(this.stationNumber + " " +
                    principal);
            }
        } else {
            if (Arrays.equals(station.getProduction(), (stateA))) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.moveFailed(this.stationNumber + " " +
                    principal);
            }
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel)w.get(KEY.STATIONS,
                    stationNumber, principal);
            station = new StationModel(station, this.after);
            w.set(KEY.STATIONS, stationNumber, station, principal);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel)w.get(KEY.STATIONS,
                    stationNumber, principal);
            station = new StationModel(station, this.before);
            w.set(KEY.STATIONS, stationNumber, station, principal);
        }

        return status;
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeProductionAtEngineShopMove) {
            ChangeProductionAtEngineShopMove arg = (ChangeProductionAtEngineShopMove)o;
            boolean stationNumbersEqual = (this.stationNumber == arg.stationNumber);
            boolean beforeFieldsEqual = Arrays.equals(this.before, arg.before);
            boolean afterFieldsEqual = Arrays.equals(this.after, arg.after);

            if (stationNumbersEqual && beforeFieldsEqual && afterFieldsEqual) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}