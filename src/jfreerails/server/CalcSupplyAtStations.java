package jfreerails.server;

import java.util.Vector;
import jfreerails.controller.CargoElementObject;
import jfreerails.move.ChangeStationMove;
import jfreerails.move.Move;
import jfreerails.network.MoveReceiver;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldListListener;


/**
 * This class loops through all of the known stations and recalculates
 * the cargoes that they supply, demand, and convert.
 *
 * @author Scott Bennett
 * Created: 19th May 2003
 */
public class CalcSupplyAtStations implements WorldListListener {
    private final World w;
    private final MoveReceiver moveReceiver;

    /**
     *
     * Constructor, currently called from GUIComponentFactory.
     *
     * @param world The World object that contains all about the game world
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
                StationModel stationBefore = (StationModel)iterator.getElement();

                StationModel stationAfter = calculations(stationBefore);

                if (!stationAfter.equals(stationBefore)) {
                    Move move = new ChangeStationMove(iterator.getIndex(),
                            stationBefore, stationAfter, principal);
                    this.moveReceiver.processMove(move);
                }
            }
        }
    }

    /**
     *
     * Process each existing station, updating what is supplied to it.
     *
     * @param station A StationModel object to be processed
     *
     */
    private StationModel calculations(StationModel station) {
        int x = station.getStationX();
        int y = station.getStationY();

        //init vars
        CalcCargoSupplyRateAtStation supplyRate;
        Vector supply = new Vector();
        int[] cargoSupplied = new int[w.size(SKEY.CARGO_TYPES)];

        //calculate the supply rates and put information into a vector
        supplyRate = new CalcCargoSupplyRateAtStation(w, x, y);
        supply = supplyRate.ScanAdjacentTiles();

        //grab the supply rates from the vector
        for (int i = 0; i < supply.size(); i++) {
            cargoSupplied[i] = ((CargoElementObject)supply.elementAt(i)).getRate();
        }

        //set the supply rates for the current station	
        SupplyAtStation supplyAtStation = new SupplyAtStation(cargoSupplied);
        station = new StationModel(station, supplyAtStation);
        station = new StationModel(station, supplyRate.getDemand());
        station = new StationModel(station, supplyRate.getConversion());

        return station;
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }
}