package jfreerails.world.common;

import java.text.DecimalFormat;


/** Represents an amount of Money.
 * @author Luke
 */
final public class Money implements FreerailsSerializable {
	
	public static final Money ZERO = new Money(0);
    private static final DecimalFormat df = new DecimalFormat("#,###");
    private final long amount;

    public long getAmount() {
        return amount;
    }

    public int hashCode() {
        return (int)(amount ^ (amount >>> 32));
    }

    public String toString() {
        return df.format(amount);
    }

    public Money(long amount) {
        this.amount = amount;
    }

    public Money changeSign() {
        return new Money(-amount);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Money) {
            Money test = (Money)obj;

            return test.amount == this.amount;
        }
		return false;
    }
}