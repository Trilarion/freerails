/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.ImList;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * This Move changes what is being built at an engine shop - when a client wants
 * to build a train, it should send an instance of this class to the server.
 * 
 * @author Luke
 * 
 */
public class ChangeProductionAtEngineShopMove implements Move {
	private static final long serialVersionUID = 3905519384997737520L;

	private final ImList<ProductionAtEngineShop> m_before;

	private final ImList<ProductionAtEngineShop> m_after;

	private final int m_stationNumber;

	private final FreerailsPrincipal m_principal;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChangeProductionAtEngineShopMove))
			return false;

		final ChangeProductionAtEngineShopMove changeProductionAtEngineShopMove = (ChangeProductionAtEngineShopMove) o;

		if (m_stationNumber != changeProductionAtEngineShopMove.m_stationNumber)
			return false;
		if (m_after != null ? !m_after
				.equals(changeProductionAtEngineShopMove.m_after)
				: changeProductionAtEngineShopMove.m_after != null)
			return false;
		if (m_before != null ? !m_before
				.equals(changeProductionAtEngineShopMove.m_before)
				: changeProductionAtEngineShopMove.m_before != null)
			return false;
		if (!m_principal.equals(changeProductionAtEngineShopMove.m_principal))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (m_before != null ? m_before.hashCode() : 0);
		result = 29 * result + (m_after != null ? m_after.hashCode() : 0);
		result = 29 * result + m_stationNumber;
		result = 29 * result + m_principal.hashCode();
		return result;
	}

	public ChangeProductionAtEngineShopMove(ImList<ProductionAtEngineShop> b,
			ImList<ProductionAtEngineShop> a, int station, FreerailsPrincipal p) {
		m_before = b;
		m_after = a;
		m_stationNumber = station;
		m_principal = p;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		return tryMove(w, m_before);
	}

	private MoveStatus tryMove(World w, ImList<ProductionAtEngineShop> stateA) {
		// Check that the specified station exists.
		if (!w.boundsContain(KEY.STATIONS, this.m_stationNumber, m_principal)) {
			return MoveStatus.moveFailed(this.m_stationNumber + " "
					+ m_principal);
		}

		StationModel station = (StationModel) w.get(KEY.STATIONS,
				m_stationNumber, m_principal);

		if (null == station) {
			return MoveStatus.moveFailed(this.m_stationNumber + " "
					+ m_principal + " is does null");
		}

		// Check that the station is building what we expect.
		if (null == station.getProduction()) {
			if (null == stateA) {
				return MoveStatus.MOVE_OK;
			}
			return MoveStatus.moveFailed(this.m_stationNumber + " "
					+ m_principal);
		}
		if (station.getProduction().equals(stateA)) {
			return MoveStatus.MOVE_OK;
		}
		return MoveStatus.moveFailed(this.m_stationNumber + " " + m_principal);
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		return tryMove(w, m_after);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryDoMove(w, p);

		if (status.isOk()) {
			StationModel station = (StationModel) w.get(KEY.STATIONS,
					m_stationNumber, m_principal);
			station = new StationModel(station, this.m_after);
			w.set(KEY.STATIONS, m_stationNumber, station, m_principal);
		}

		return status;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryUndoMove(w, p);

		if (status.isOk()) {
			StationModel station = (StationModel) w.get(KEY.STATIONS,
					m_stationNumber, m_principal);
			station = new StationModel(station, this.m_before);
			w.set(KEY.STATIONS, m_stationNumber, station, m_principal);
		}

		return status;
	}

}