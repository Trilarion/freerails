package freerails.model;

import java.io.Serializable;

/**
 * All objects derived from Identifiable must not implement their own equals() or hashCode() functions. The idea is that
 * objects with the same id are equal. Objects of that type are typically used in some sort of set and can be identified
 * by their id from outside.
 */
public class Identifiable implements Comparable<Identifiable>, Serializable {

    private final int id;

    /**
     * Parameter-less constructor used for deserialization.
     */
    public Identifiable() {
        this(-1);
    }

    /**
     *
     * @param id
     */
    public Identifiable(int id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Identifiable) {
            return this.compareTo((Identifiable) obj) == 0;
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public final int hashCode() {
        return id;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Identifiable o) {
        return Integer.compare(id, o.id);
    }

    /**
     *
     * @param id
     * @param list
     * @param <T>
     * @return
     */
    public static <T extends Identifiable> T getById(int id, Iterable<T> list) {
        for (T t: list) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }
}

