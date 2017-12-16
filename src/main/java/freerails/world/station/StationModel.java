package freerails.world.station;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImList;

/**
 * This class represents a station.
 * 
 * @author Luke
 * 
 */
public class StationModel implements FreerailsSerializable {
    private static final long serialVersionUID = 3256442503979874355L;

    public final int x;

    public final int y;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StationModel))
            return false;

        final StationModel stationModel = (StationModel) o;

        if (cargoBundleNumber != stationModel.cargoBundleNumber)
            return false;
        if (x != stationModel.x)
            return false;
        if (y != stationModel.y)
            return false;
        if (converted != null ? !converted.equals(stationModel.converted)
                : stationModel.converted != null)
            return false;
        if (demand != null ? !demand.equals(stationModel.demand)
                : stationModel.demand != null)
            return false;
        if (!name.equals(stationModel.name))
            return false;
        if (production != null ? !production.equals(stationModel.production)
                : stationModel.production != null)
            return false;
        if (supply != null ? !supply.equals(stationModel.supply)
                : stationModel.supply != null)
            return false;

        return true;
    }

    private final String name;

    private final SupplyAtStation supply;

    private final Demand4Cargo demand;

    private final ConvertedAtStation converted;

    private final int cargoBundleNumber;

    /** What this station is building. */
    private final ImList<PlannedTrain> production;

    public ConvertedAtStation getConverted() {
        return converted;
    }

    @Override
    public int hashCode() {
        int result;
        result = x;
        result = 29 * result + y;
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (supply != null ? supply.hashCode() : 0);
        result = 29 * result + (demand != null ? demand.hashCode() : 0);
        result = 29 * result + (converted != null ? converted.hashCode() : 0);
        result = 29 * result + cargoBundleNumber;
        result = 29 * result + production.size();

        return result;
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
        production = new ImList<PlannedTrain>();

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new Demand4Cargo(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
    }

    public StationModel() {
        this.name = "No name";
        x = 0;
        y = 0;
        this.demand = new Demand4Cargo(new boolean[0]);
        this.supply = new SupplyAtStation(new int[0]);
        this.converted = new ConvertedAtStation(new int[0]);
        production = new ImList<PlannedTrain>();
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

    public ImList<PlannedTrain> getProduction() {
        return production;
    }

    public StationModel(StationModel s, ImList<PlannedTrain> production) {
        this.production = production;
        this.demand = s.demand;
        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;
        this.name = s.name;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
    }

    public Demand4Cargo getDemand() {
        return demand;
    }

    public SupplyAtStation getSupply() {
        return supply;
    }

    public StationModel(StationModel s, Demand4Cargo demand) {
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

    public int getCargoBundleID() {
        return cargoBundleNumber;
    }

}