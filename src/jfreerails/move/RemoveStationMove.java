/*
 * Created on 15-Apr-2003
 * 
 */
package jfreerails.move;

import java.awt.Rectangle;

import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;

/**
 * This Move removes a station from the station list and from the map.
 * @author Luke
 * 
 */
public class RemoveStationMove extends RemoveItemFromListMove implements TrackMove {

	private final ChangeTrackPieceMove trackMove; //this move removes the station from the map.

	private RemoveStationMove(
		int index,
		StationModel station,
		ChangeTrackPieceMove removeTrackMove) {
		super(KEY.STATIONS, index, station);
		trackMove = removeTrackMove;		
	}

	static RemoveStationMove getInstance(ReadOnlyWorld w, ChangeTrackPieceMove removeTrackMove) {
		WorldIterator wi = new NonNullElements(KEY.STATIONS, w);
		int stationIndex = -1;
		while (wi.next()) {
			StationModel station = (StationModel) wi.getElement();
			if (station.x == removeTrackMove.getLocation().x
				&& station.y == removeTrackMove.getLocation().y) {
				//We have found the station!
				stationIndex = wi.getIndex();
				break;
			}
		}
		if (-1 == stationIndex) {
			throw new IllegalArgumentException(
				"Could find a station at "
					+ removeTrackMove.getLocation().x
					+ ", "
					+ removeTrackMove.getLocation().y);
		}
		StationModel station2remove = (StationModel) w.get(KEY.STATIONS, stationIndex);
		return new RemoveStationMove(stationIndex, station2remove, removeTrackMove);
	}

	public Rectangle getUpdatedTiles() {
		return trackMove.getUpdatedTiles();
	}

	public MoveStatus tryDoMove(World w) {
		MoveStatus ms = trackMove.tryDoMove(w);
		if (!ms.ok) {
			return ms;
		} else {
			return super.tryDoMove(w);
		}
	}

	public MoveStatus tryUndoMove(World w) {
		MoveStatus ms = trackMove.tryUndoMove(w);
		if (!ms.ok) {
			return ms;
		} else {
			return super.tryUndoMove(w);
		}
	}

	public MoveStatus doMove(World w) {
		MoveStatus ms = this.tryDoMove(w);
		if (ms.isOk()) {
			super.doMove(w);
			trackMove.doMove(w);
		}
		return ms;
	}

	public MoveStatus undoMove(World w) {
		MoveStatus ms = this.tryUndoMove(w);
		if (ms.isOk()) {
			super.undoMove(w);
			trackMove.undoMove(w);
		}
		return ms;
	}
}
