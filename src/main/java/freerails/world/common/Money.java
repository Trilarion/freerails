package freerails.world.common;

import java.text.DecimalFormat;

/**
 * Represents an amount of Money.
 *
 * @author Luke
 */
final public class Money implements FreerailsSerializable {

    private static final long serialVersionUID = 3258697615163338805L;

    public static final Money ZERO = new Money(0);

    private static final DecimalFormat df = new DecimalFormat("#,###");

    private final long amount;

    public long getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return (int) (amount ^ (amount >>> 32));
    }

    @Override
    public String toString() {
        return df.format(amount);
    }

    public Money(long amount) {
        this.amount = amount;
    }

    public Money changeSign() {
        return new Money(-amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Money) {
            Money test = (Money) obj;

            return test.amount == this.amount;
        }
        return false;
    }
}