package freerails.server;

import freerails.controller.CalcCargoSupplyRateAtStation;
import freerails.move.ChangeStationMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.NonNullElements;
import freerails.world.top.World;

/**
 * This class loops through all of the known stations and recalculates the
 * cargoes that they supply, demand, and convert.
 *
 */
public class CalcSupplyAtStations {
    private final World w;

    private final MoveReceiver moveReceiver;

    /**
     * Constructor, currently called from GUIComponentFactory.
     *
     * @param world The World object that contains all about the game world
     * @param mr
     */
    public CalcSupplyAtStations(World world, MoveReceiver mr) {
        this.w = world;
        this.moveReceiver = mr;
    }

    /**
     * Loop through each known station, call calculations method.
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