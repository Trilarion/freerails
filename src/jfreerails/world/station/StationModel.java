package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;


/**
 * This class represents a station.
 *
 * @author Luke
 *
 */
public class StationModel implements FreerailsSerializable {
    public int x;
    public int y;
    private final String name;
    private SupplyAtStation supply;
    private DemandAtStation demand;
    private ConvertedAtStation converted;
    private int cargoBundleNumber;

    /** What this station is building. */
    private ProductionAtEngineShop production;

    public ConvertedAtStation getConverted() {
        return converted;
    }

    public void setConverted(ConvertedAtStation converted) {
        this.converted = converted;
    }

    public StationModel(int x, int y, String stationName,
        int numberOfCargoTypes, int cargoBundle) {
        this.name = stationName;
        this.x = x;
        this.y = y;

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new DemandAtStation(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
    }

    public StationModel() {
        this.name = "No name";
        x = 0;
        y = 0;
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

    public void setProduction(ProductionAtEngineShop production) {
        this.production = production;
    }

    public DemandAtStation getDemand() {
        return demand;
    }

    public SupplyAtStation getSupply() {
        return supply;
    }

    public int getWaiting() {
        return cargoBundleNumber;
    }

    public void setDemand(DemandAtStation demand) {
        this.demand = demand;
    }

    public void setSupply(SupplyAtStation supply) {
        this.supply = supply;
    }

    public void setWaiting(int waiting) {
        this.cargoBundleNumber = waiting;
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