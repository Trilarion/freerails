/*
 * Created on 07-Jul-2005
 *
 */
package freerails.world.common;

import freerails.util.Immutable;

import java.util.Arrays;

/**
 * An immutable list of Strings.
 *
 * @author Luke
 */
@Immutable
public class ImStringList implements FreerailsSerializable {

    private static final long serialVersionUID = 5211786598838212188L;

    private final String[] strings;

    public ImStringList(String... strings) {
        this.strings = strings.clone();
    }

    public String get(int i) {
        return strings[i];
    }

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

        if (!Arrays.equals(strings, imStringList.strings))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return strings.length;
    }

}
