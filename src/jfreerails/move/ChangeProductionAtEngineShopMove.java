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
    private final ProductionAtEngineShop[] m_before;
    private final ProductionAtEngineShop[] m_after;
    private final int m_stationNumber;
    private final FreerailsPrincipal m_principal;

    public int hashCode() {
        int result;
        result = m_before.length;
        result = 29 * result + m_after.length;
        result = 29 * result + m_stationNumber;
        result = 29 * result + m_principal.hashCode();

        return result;
    }

    public ChangeProductionAtEngineShopMove(ProductionAtEngineShop[] b,
        ProductionAtEngineShop[] a, int station, FreerailsPrincipal p) {
        m_before = b.clone();
        m_after = a.clone();
        m_stationNumber = station;
        m_principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, m_before);
    }

    private MoveStatus tryMove(World w, /*=const*/
        ProductionAtEngineShop[] stateA) {
        //Check that the specified station exists.
        if (!w.boundsContain(KEY.STATIONS, this.m_stationNumber, m_principal)) {
            return MoveStatus.moveFailed(this.m_stationNumber + " " +
                m_principal);
        }

        StationModel station = (StationModel)w.get(KEY.STATIONS,
                m_stationNumber, m_principal);

        if (null == station) {
            return MoveStatus.moveFailed(this.m_stationNumber + " " +
                m_principal + " is does null");
        }

        //Check that the station is building what we expect.					
        if (null == station.getProduction()) {
            if (null == stateA) {
                return MoveStatus.MOVE_OK;
            }
			return MoveStatus.moveFailed(this.m_stationNumber + " " +
			    m_principal);
        }
		if (Arrays.equals(station.getProduction(), (stateA))) {
		    return MoveStatus.MOVE_OK;
		}
		return MoveStatus.moveFailed(this.m_stationNumber + " " +
		    m_principal);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, m_after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel)w.get(KEY.STATIONS,
                    m_stationNumber, m_principal);
            station = new StationModel(station, this.m_after);
            w.set(KEY.STATIONS, m_stationNumber, station, m_principal);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            StationModel station = (StationModel)w.get(KEY.STATIONS,
                    m_stationNumber, m_principal);
            station = new StationModel(station, this.m_before);
            w.set(KEY.STATIONS, m_stationNumber, station, m_principal);
        }

        return status;
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeProductionAtEngineShopMove) {
            ChangeProductionAtEngineShopMove arg = (ChangeProductionAtEngineShopMove)o;
            boolean stationNumbersEqual = (this.m_stationNumber == arg.m_stationNumber);
            boolean beforeFieldsEqual = Arrays.equals(this.m_before,
                    arg.m_before);
            boolean afterFieldsEqual = Arrays.equals(this.m_after, arg.m_after);

            if (stationNumbersEqual && beforeFieldsEqual && afterFieldsEqual) {
                return true;
            }
			return false;
        }
		return false;
    }
}