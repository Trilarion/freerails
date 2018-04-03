package freerails.model.track.pathfinding;

import java.io.Serializable;

public class OpenListEntry implements Comparable<OpenListEntry>, Serializable {

    private static final long serialVersionUID = -4873508719707382681L;
    final int f;
    final int node;

    OpenListEntry(int f, int node) {
        this.f = f;
        this.node = node;
    }

    public int compareTo(OpenListEntry o) {
        if (f == o.f) {
            return node - o.node;
        }
        return f - o.f;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OpenListEntry)) return false;
        final OpenListEntry other = (OpenListEntry) obj;

        return (f == other.f && node == other.node);
    }

    @Override
    public int hashCode() {
        int result = f;
        result = 29 * result + node;
        return result;
    }

    @Override
    public String toString() {
        return "OpenListEntry{node=" + node + ", f=" + f + '}';
    }

}
