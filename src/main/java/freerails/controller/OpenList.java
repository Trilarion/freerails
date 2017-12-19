package freerails.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * An OpenList for SimpleAStarPathFinder.
 *
 */
class OpenList implements Serializable {

    private static final long serialVersionUID = 3257282539419611442L;
    private final HashMap<Integer, OpenListEntry> map = new HashMap<>();
    private final PriorityQueue<OpenListEntry> queue = new PriorityQueue<>();

    public OpenList() {
    }

    void clear() {
        queue.clear();
        map.clear();
    }

    int getF(int node) {
        return map.get(node).f;
    }

    void add(int node, int f) {

        if (map.containsKey(node)) {
            OpenListEntry old = map.get(node);

            queue.remove(old);

            map.remove(node);

        }
        OpenList.OpenListEntry entry = new OpenListEntry(f, node);
        queue.offer(entry);
        map.put(node, entry);

    }

    boolean contains(int node) {
        return map.containsKey(node);
    }

    int smallestF() {
        OpenListEntry entry = queue.peek();
        return entry.f;
    }

    int popNodeWithSmallestF() {
        OpenListEntry entry = queue.remove();
        int node = entry.node;
        OpenListEntry removed = map.remove(node);

        if (null == removed) {
            System.out.println("Shizer, size =" + queue.size());
        }
        return node;
    }

    int size() {
        return queue.size();
    }

    static class OpenListEntry implements Comparable<OpenListEntry>,
            Serializable {
        private static final long serialVersionUID = -4873508719707382681L;

        final int f;

        final int node;

        OpenListEntry(int _f, int _node) {
            this.f = _f;
            this.node = _node;
        }

        public int compareTo(OpenListEntry o) {
            // XXX Work around for JDK Bug ID: 6207984
            if (f == o.f) {
                return node - o.node;
            }
            return f - o.f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof OpenListEntry))
                return false;

            final OpenListEntry openListEntry = (OpenListEntry) o;

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
            return "OpenListEntry{node=" + node + ", f=" + f + "}";
        }

    }

}