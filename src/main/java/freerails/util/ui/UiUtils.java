package freerails.util.ui;

/**
 *
 */
public final class UiUtils {

    private UiUtils() {}

    /**
     *
     * @param colorString
     * @return
     */
    public static int stringToRGBValue(String colorString) {
        int rgb = Integer.parseInt(colorString, 16);

        /*
         * We need to change the format of the rgb value to the same one as used
         * by the the BufferedImage that stores the map.
         */
        rgb = new java.awt.Color(rgb).getRGB();

        return rgb;
    }
}
