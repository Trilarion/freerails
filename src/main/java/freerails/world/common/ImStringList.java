/*
 * Created on 07-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;
import freerails.world.FreerailsSerializable;

import java.util.Arrays;

/**
 * An immutable list of Strings.
 *
 */
@Immutable
public class ImStringList implements FreerailsSerializable {

    private static final long serialVersionUID = 5211786598838212188L;

    private final String[] strings;

    /**
     *
     * @param strings
     */
    public ImStringList(String... strings) {
        this.strings = strings.clone();
    }

    /**
     *
     * @param i
     * @return
     */
    public String get(int i) {
        return strings[i];
    }

    /**
     *
     * @return
     */
    public int size() {
        return strings.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ImStringList))
            return false;

        final ImStringList imStringList = (ImStringList) o;

        return Arrays.equals(strings, imStringList.strings);
    }

    @Override
    public int hashCode() {
        return strings.length;
    }

}
