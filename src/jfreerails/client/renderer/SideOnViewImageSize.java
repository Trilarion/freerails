package jfreerails.client.renderer;

import java.util.HashSet;
import java.util.Set;


public class SideOnViewImageSize {
    private static final Set set = new HashSet();
    public static final SideOnViewImageSize TINY = new SideOnViewImageSize(10);
    private final double scale;

    private SideOnViewImageSize(double s) {
        scale = s;
        set.add(this);
    }

    public int getWidth() {
        return (int)scale;
    }

    public int getHeight() {
        return (int)scale / 2;
    }
}