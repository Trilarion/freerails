/*
 * BinaryNumberFormatter.java
 *
 * Created on 30 April 2003, 20:23
 */
package freerails.client.common;

/**
 * Used to generate file names for track and terrain images.
 *
 */
public class BinaryNumberFormatter {

    /**
     *
     * @param i
     * @param bits
     * @return
     */
    public static String format(int i, int bits) {
        int maxValue = 1 << (bits);

        if (i < 0) {
            throw new IllegalArgumentException(
                    "i must be greater than 0.  It was " + i);
        }

        if (i >= maxValue) {
            throw new IllegalArgumentException("i must be less than "
                    + maxValue + ".  It was " + i + " ("
                    + Integer.toString(i, 2) + ")");
        }

        String s = Integer.toString(i + maxValue, 2);

        return s.substring(1);
    }

    /**
     *
     * @param i
     * @param bits
     * @return
     */
    public static String formatWithLowBitOnLeft(int i, int bits) {
        StringBuilder buff = new StringBuilder(format(i, bits));
        buff.reverse();

        return buff.toString();
    }
}