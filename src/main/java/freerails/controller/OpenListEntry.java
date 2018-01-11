package freerails.controller;

import java.io.Serializable;

class OpenListEntry implements Comparable<OpenListEntry>,
        Serializable {
    private static final long serialVersionUID = -4873508719707382681L;

    final int f;

    final int node;

    OpenListEntry(int _f, int _node) {
        f = _f;
        node = _node;
    }

    public int compareTo(OpenListEntry o) {
        // XXX Work around for JDK Bug ID: 6207984
        if (f == o.f) {
            return node - o.node;
        }
        return f - o.f;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof OpenListEntry))
            return false;

        final OpenListEntry openListEntry = (OpenListEntry) obj;

        if (f != openListEntry.f)
            return false;
        return node == openListEntry.node;
    }

    @Override
    public int hashCode() {
        int result;
        result = f;
        result = 29 * result + node;
        return result;
    }

    @Override
    public String toString() {
        return "OpenListEntry{node=" + node + ", f=" + f + '}';
    }

}
