package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


/**
 * This class represents a station.
 *
 * @author Luke
 *
 */
public class StationModel implements FreerailsSerializable {
    public final int x;
    public final int y;
    private final String name;
    private final SupplyAtStation supply;
    private final DemandAtStation demand;
    private final ConvertedAtStation converted;
    private final int cargoBundleNumber;

    /** What this station is building. */
    private final ProductionAtEngineShop production;

    public ConvertedAtStation getConverted() {
        return converted;
    }

    public StationModel(StationModel s, ConvertedAtStation converted) {
        this.converted = converted;

        this.cargoBundleNumber = s.cargoBundleNumber;

        this.demand = s.demand;
        this.name = s.name;
        this.production = s.production;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
    }

    public StationModel(int x, int y, String stationName,
        int numberOfCargoTypes, int cargoBundle) {
        this.name = stationName;
        this.x = x;
        this.y = y;
        production = null;

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new DemandAtStation(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
    }

    public StationModel() {
        this.name = "No name";
        x = 0;
        y = 0;
        this.demand = new DemandAtStation(new boolean[0]);
        this.supply = new SupplyAtStation(new int[0]);
        this.converted = new ConvertedAtStation(new int[0]);
        production = null;
        this.cargoBundleNumber = 0;
    }

    public String getStationName() {
        return name;
    }

    public int getStationX() {
        return x;
    }

    public int getStationY() {
        return y;
    }

    public ProductionAtEngineShop getProduction() {
        return production;
    }

    public StationModel(StationModel s, ProductionAtEngineShop production) {
        this.production = production;
        this.demand = s.demand;
        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;
        this.name = s.name;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
    }

    public DemandAtStation getDemand() {
        return demand;
    }

    public SupplyAtStation getSupply() {
        return supply;
    }

    public StationModel(StationModel s, DemandAtStation demand) {
        this.demand = demand;

        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;

        this.name = s.name;
        this.production = s.production;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
    }

    public StationModel(StationModel s, SupplyAtStation supply) {
        this.supply = supply;
        this.demand = s.demand;

        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;
        this.name = s.name;
        this.production = s.production;
        this.x = s.x;
        this.y = s.y;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public boolean equals(Object o) {
        if (o instanceof StationModel) {
            StationModel test = (StationModel)o;

            if (this.cargoBundleNumber != test.cargoBundleNumber) {
                return false;
            }

            if (!this.demand.equals(test.demand)) {
                return false;
            }

            if (!this.converted.equals(test.converted)) {
                return false;
            }

            if (!this.name.equals(test.name)) {
                return false;
            }

            if (!(this.production == null ? test.production == null
                                              : this.production.equals(
                        test.production))) {
                return false;
            }

            if (!this.supply.equals(test.supply)) {
                return false;
            }

            if (this.x != test.x || this.y != test.y) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}