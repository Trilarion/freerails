package jfreerails.server;

import jfreerails.controller.CalcCargoSupplyRateAtStation;
import jfreerails.move.ChangeStationMove;
import jfreerails.move.Move;
import jfreerails.network.MoveReceiver;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;

/**
 * This class loops through all of the known stations and recalculates the
 * cargoes that they supply, demand, and convert.
 * 
 * @author Scott Bennett Created: 19th May 2003
 */
public class CalcSupplyAtStations {
	private final World w;

	private final MoveReceiver moveReceiver;

	/**
	 * 
	 * Constructor, currently called from GUIComponentFactory.
	 * 
	 * @param world
	 *            The World object that contains all about the game world
	 * 
	 */
	public CalcSupplyAtStations(World world, MoveReceiver mr) {
		this.w = world;
		this.moveReceiver = mr;
	}

	/**
	 * 
	 * Loop through each known station, call calculations method.
	 * 
	 */
	public void doProcessing() {
		for (int i = 0; i < w.getNumberOfPlayers(); i++) {
			FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
			NonNullElements iterator = new NonNullElements(KEY.STATIONS, w,
					principal);

			while (iterator.next()) {
				StationModel stationBefore = (StationModel) iterator
						.getElement();
				CalcCargoSupplyRateAtStation supplyRate;
				supplyRate = new CalcCargoSupplyRateAtStation(w,
						stationBefore.x, stationBefore.y);

				StationModel stationAfter = supplyRate
						.calculations(stationBefore);

				if (!stationAfter.equals(stationBefore)) {
					Move move = new ChangeStationMove(iterator.getIndex(),
							stationBefore, stationAfter, principal);
					this.moveReceiver.processMove(move);
				}
			}
		}
	}
}