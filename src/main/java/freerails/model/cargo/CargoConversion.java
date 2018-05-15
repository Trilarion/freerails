package freerails.model.cargo;

import java.io.Serializable;

// TODO equals, hashcode
/**
 *
 */
public class CargoConversion implements Serializable {

    private final int sourceCargoId;
    private final int productCargoId;
    private final double conversionRate;

    public CargoConversion(int sourceCargoId, int productCargoId, double conversionRate) {
        if (conversionRate < 0) {
            throw new RuntimeException("conversion rate cannot be negative.");
        }
        this.sourceCargoId = sourceCargoId;
        this.productCargoId = productCargoId;
        this.conversionRate = conversionRate;
    }

    public int getSourceCargoId() {
        return sourceCargoId;
    }

    public int getProductCargoId() {
        return productCargoId;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CargoConversion) {
            CargoConversion other = (CargoConversion) obj;
            return sourceCargoId == other.sourceCargoId && productCargoId == other.productCargoId && conversionRate == other.conversionRate;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = sourceCargoId;
        result = 31 * result + productCargoId;
        result = 31 * result + Double.hashCode(conversionRate);
        return result;
    }
}
