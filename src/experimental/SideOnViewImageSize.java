package experimental;

import java.util.HashSet;
import java.util.Set;


public class SideOnViewImageSize {
    private static final Set set = new HashSet();
    private final double scale;

    SideOnViewImageSize(double s) {
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