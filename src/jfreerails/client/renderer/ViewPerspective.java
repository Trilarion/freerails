package jfreerails.client.renderer;

import java.util.HashSet;
import java.util.Set;


public class ViewPerspective {
    private static final Set set = new HashSet();
    public static final ViewPerspective OVERHEAD = new ViewPerspective();

    private ViewPerspective() {
        set.add(this);
    }
}