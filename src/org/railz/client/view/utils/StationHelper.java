package org.railz.client.view.utils;

import org.railz.client.model.ModelRoot;
import org.railz.world.building.BuildingTile;
import org.railz.world.building.BuildingType;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.FreerailsTile;

public class StationHelper {

	public static Integer retrieveStationAtLocation(int x, int y,
			ReadOnlyWorld world, ModelRoot modelRoot) {
		Integer stationNumber = null;
		FreerailsTile tile = world.getTile(x, y);
		BuildingTile bTile = tile.getBuildingTile();
		if (bTile != null) {
			BuildingType bType = (BuildingType) world.get(KEY.BUILDING_TYPES,
					tile.getBuildingTile().getType(), Player.AUTHORITATIVE);

			if (bType.getCategory() == BuildingType.CATEGORY_STATION) {
				for (int i = 0; i < world.size(KEY.STATIONS,
						modelRoot.getPlayerPrincipal()); i++) {
					StationModel station = (StationModel) world.get(
							KEY.STATIONS, i, modelRoot.getPlayerPrincipal());
					if (null != station && station.x == x && station.y == y) {
						stationNumber = i;
						return stationNumber;
					}
				}
				throw new IllegalStateException("Could find station at " + x
						+ ", " + y);
			}
		}
		return stationNumber;
	}

	public static BuildingTile getBuildingAtLocation(int x, int y,
			ReadOnlyWorld world) {
		// BuildingType bType = null;
		FreerailsTile tile = world.getTile(x, y);
		BuildingTile bTile = tile.getBuildingTile();
		// if (bTile != null) {
		// bType = (BuildingType) world.get(KEY.BUILDING_TYPES,
		// tile.getTrackRule(), Player.AUTHORITATIVE);
		// }
		return bTile;
	}
}
