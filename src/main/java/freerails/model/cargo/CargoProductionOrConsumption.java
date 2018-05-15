package freerails.model.cargo;

import java.io.Serializable;

// TODO equals, hashcode
/**
 *
 */
public final class CargoProductionOrConsumption implements Serializable {

    private final int cargoId;
    private final double rate;

    public CargoProductionOrConsumption(int cargoId, double rate) {
        if (rate < 0) {
            throw new RuntimeException("rate cannot be negative.");
        }
        this.cargoId = cargoId;
        this.rate = rate;
    }

    public int getCargoId() {
        return cargoId;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoProductionOrConsumption) {
            CargoProductionOrConsumption other = (CargoProductionOrConsumption) obj;
            return cargoId == other.cargoId && rate == other.rate;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cargoId + 31 * Double.hashCode(rate);
    }
}
