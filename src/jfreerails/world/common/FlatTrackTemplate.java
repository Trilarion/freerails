package jfreerails.world.common;


/**
 * This interface ...
 */
public interface FlatTrackTemplate extends FreerailsSerializable {
    /**
     * constants for ease of sanity when understanding output of getTemplate()
     */
    public static final int TEMPLATE_NW = 0x01;
    public static final int TEMPLATE_N = 0x02;
    public static final int TEMPLATE_NE = 0x04;
    public static final int TEMPLATE_W = 0x08;
    public static final int TEMPLATE_CENTRE = 0x10;
    public static final int TEMPLATE_E = 0x20;
    public static final int TEMPLATE_SW = 0x40;
    public static final int TEMPLATE_S = 0x80;
    public static final int TEMPLATE_SE = 0x100;

    /**
     * constants for ease of sanity when understanding output of
     * getNewTemplateNumber()
     */
    public static final int NEW_TEMPLATE_N = 0x01;
    public static final int NEW_TEMPLATE_NE = 0x02;
    public static final int NEW_TEMPLATE_E = 0x04;
    public static final int NEW_TEMPLATE_SE = 0x08;
    public static final int NEW_TEMPLATE_S = 0x10;
    public static final int NEW_TEMPLATE_SW = 0x20;
    public static final int NEW_TEMPLATE_W = 0x40;
    public static final int NEW_TEMPLATE_NW = 0x80;

    /**
     * @return the corresponding instance of this object when rotated by the
     * specified amount
     */
    FlatTrackTemplate getRotatedInstance(Rotation r);

    /**
     * @param ftt the FlatTrackTemplate which may be a subset of this
     * FlatTrackTemplate.
     * @return true if the vectors represented by this FlatTrackTemplate are a
     * superset of the vectors of the specified FlatTrackTemplate
     */
    boolean contains(FlatTrackTemplate ftt);

    /**
     * @return the integer representing the vector(s) of this object.
     */
    int getTemplate();

    /**
     * @return the integer representing the vector(s) of this object.
     */
    int getNewTemplateNumber();
}