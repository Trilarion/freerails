/*
 * MyDisplayMode.java
 *
 * Created on 31 August 2003, 00:03
 */
package freerails.controller;

import java.awt.*;

/**
 * Stores a DisplayMode and provides a customised implementation of toString
 * that can be used in menus.
 *
 * @author Luke Lindsay
 */
public class MyDisplayMode {
    public final DisplayMode displayMode;

    public MyDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public String toString() {
        return displayMode.getWidth() + "x" + displayMode.getHeight() + " "
                + displayMode.getBitDepth() + " bit "
                + displayMode.getRefreshRate() + "Hz";
    }

    @Override
    public int hashCode() {
        return displayMode.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MyDisplayMode) {
            MyDisplayMode test = (MyDisplayMode) o;

            return test.displayMode.equals(this.displayMode);
        }
        return false;
    }
}