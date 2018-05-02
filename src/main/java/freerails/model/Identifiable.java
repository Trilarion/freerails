package freerails.model;

/**
 *
 */
public class Identifiable implements Comparable<Identifiable> {

    private final int id;

    public Identifiable() {
        this(0);
    }

    public Identifiable(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Identifiable) {
            Identifiable other = (Identifiable) obj;
            return id == other.id;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return id;
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

    @Override
    public int compareTo(Identifiable o) {
        return Integer.compare(id, o.id);
    }
}

