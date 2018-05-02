package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.finances.Money;

import java.io.Serializable;

/**
 *
 */
public class Engine extends Identifiable implements Serializable {

    private final String name;
    private final Money price;
    private final Money upkeep;
    private final int maximumSpeed;
    private final int maximumThrust;

    public Engine(int id, String name, Money price, Money upkeep, int maximumSpeed, int maximumThrust) {
        super(id);
        this.name = name;
        this.price = price;
        this.upkeep = upkeep;
        this.maximumSpeed = maximumSpeed;
        this.maximumThrust = maximumThrust;
    }


    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public Money getUpkeep() {
        return upkeep;
    }

    public int getMaximumSpeed() {
        return maximumSpeed;
    }

    public int getMaximumThrust() {
        return maximumThrust;
    }
}
