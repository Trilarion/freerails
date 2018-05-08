package freerails.client;

/**
 * ARGB value in an int. Needs its own class, so gson can convert to/from hex value in json de/serialization.
 */
public class ARGBColor {

    private int argb;

    public ARGBColor(int argb) {
        this.argb = argb;
    }

    public int getARGB() {
        return argb;
    }

    public static String toHexString(ARGBColor color) {
        return String.format("#%s", Integer.toHexString(color.getARGB()));
    }

    public static ARGBColor fromHexString(String hexString) {
        if (!(hexString.charAt(0) == '#')) {
            throw new IllegalArgumentException();
        }
        int argb = Integer.parseUnsignedInt(hexString.substring(1), 16);
        return new ARGBColor(argb);
    }
}
