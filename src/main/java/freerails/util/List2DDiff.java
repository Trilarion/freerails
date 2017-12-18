/*
 * Created on 26-Jul-2005
 *
 */
package freerails.util;

import java.util.SortedMap;

public class List2DDiff<T> extends ListXDDiffs<T> implements List2D<T> {

    private static final long serialVersionUID = 4323585276281406244L;
    private final List2D<T> underlyingList;

    public List2DDiff(SortedMap<ListKey, Object> diffs, List2D<T> list,
                      Enum listID) {
        super(diffs, listID);
        underlyingList = list;
    }

    public int sizeD1() {
        return super.size();
    }

    public int sizeD2(int d1) {
        return super.size(d1);
    }

    public T get(int d1, int d2) {
        return super.get(d1, d2);
    }

    public T removeLastD2(int d1) {
        return super.removeLast(d1);
    }

    public int removeLastD1() {
        return super.removeLastList();
    }

    public int addD1() {
        return super.addDimension();
    }

    public int addD2(int d1, T element) {
        return super.addElement(element, d1);
    }

    public void set(int d1, int d2, T element) {
        super.set(element, d1, d2);

    }

    @Override
    Object getUnderlyingList() {
        return underlyingList;
    }

    @Override
    T uGet(int... i) {
        if (i.length != 2)
            throw new IllegalArgumentException(String.valueOf(i.length));
        return underlyingList.get(i[0], i[1]);
    }

    @Override
    int getUnderlyingSize(int... dim) {
        if (dim.length == 0)
            return underlyingList.sizeD1();
        if (dim.length == 1) {
            if (underlyingList.sizeD1() <= dim[0])
                return -1;

            return underlyingList.sizeD2(dim[0]);
        }
        throw new IllegalArgumentException(String.valueOf(dim.length));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof List2D && Lists.equals(this, (List2D) obj);
    }

    @Override
    public int hashCode() {
        return sizeD1();
    }

}
