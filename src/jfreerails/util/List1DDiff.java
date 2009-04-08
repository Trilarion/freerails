/*
 * Created on 27-Jul-2005
 *
 */
package jfreerails.util;

import java.util.SortedMap;

public class List1DDiff<T> extends ListXDDiffs<T> implements List1D<T> {

	private static final long serialVersionUID = -6058018396890452219L;

	private final List1D<T> underlyingList;

	public List1DDiff(SortedMap<ListKey, Object> diffs, List1D<T> list,
			Enum listID) {
		super(diffs, listID);
		underlyingList = list;
	}

	public T get(int i) {
		return get(new int[] { i });
	}

	@Override
	Object getUnderlyingList() {
		return underlyingList;
	}

	public int size() {
		return super.size(new int[0]);
	}

	@Override
	T uGet(int... i) {
		if (i.length != 1)
			throw new IllegalArgumentException();
		return underlyingList.get(i[0]);
	}

	public int add(T element) {
		return super.addElement(element);
	}

	public T removeLast() {
		return super.removeLast();
	}

	public void set(int i, T element) {
		super.set(element, i);

	}

	@Override
	int getUnderlyingSize(int... dim) {
		if (dim.length != 0)
			throw new IllegalArgumentException(String.valueOf(dim.length));

		return underlyingList.size();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof List1D))
			return false;
		return Lists.equals(this, (List1D) obj);
	}

	@Override
	public int hashCode() {
		return size();
	}

}
