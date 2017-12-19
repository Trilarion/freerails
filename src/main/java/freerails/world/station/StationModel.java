package freerails.world.station;

import freerails.world.FreerailsSerializable;
import freerails.world.common.ImList;

/**
 * This class represents a station.
 *
 */
public class StationModel implements FreerailsSerializable {
    private static final long serialVersionUID = 3256442503979874355L;

    /**
     *
     */
    public final int x;

    /**
     *
     */
    public final int y;
    private final String name;
    private final SupplyAtStation supply;
    private final DemandForCargo demand;
    private final ConvertedAtStation converted;
    private final int cargoBundleNumber;
    /**
     * What this station is building.
     */
    private final ImList<PlannedTrain> production;

    /**
     *
     * @param s
     * @param converted
     */
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

    /**
     *
     * @param x
     * @param y
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBundle
     */
    public StationModel(int x, int y, String stationName,
                        int numberOfCargoTypes, int cargoBundle) {
        this.name = stationName;
        this.x = x;
        this.y = y;
        production = new ImList<>();

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new DemandForCargo(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
    }

    /**
     *
     */
    public StationModel() {
        this.name = "No name";
        x = 0;
        y = 0;
        this.demand = new DemandForCargo(new boolean[0]);
        this.supply = new SupplyAtStation(new int[0]);
        this.converted = new ConvertedAtStation(new int[0]);
        production = new ImList<>();
        this.cargoBundleNumber = 0;
    }

    /**
     *
     * @param s
     * @param production
     */
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

    /**
     *
     * @param s
     * @param demand
     */
    public StationModel(StationModel s, DemandForCargo demand) {
        this.demand = demand;

        this.cargoBundleNumber = s.cargoBundleNumber;
        this.converted = s.converted;

        this.name = s.name;
        this.production = s.production;
        this.supply = s.supply;
        this.x = s.x;
        this.y = s.y;
    }

    /**
     *
     * @param s
     * @param supply
     */
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
        return supply != null ? supply.equals(stationModel.supply) : stationModel.supply == null;
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    public String getStationName() {
        return name;
    }

    /**
     *
     * @return
     */
    public int getStationX() {
        return x;
    }

    /**
     *
     * @return
     */
    public int getStationY() {
        return y;
    }

    /**
     *
     * @return
     */
    public ImList<PlannedTrain> getProduction() {
        return production;
    }

    /**
     *
     * @return
     */
    public DemandForCargo getDemand() {
        return demand;
    }

    /**
     *
     * @return
     */
    public SupplyAtStation getSupply() {
        return supply;
    }

    /**
     *
     * @return
     */
    public int getCargoBundleID() {
        return cargoBundleNumber;
    }

}