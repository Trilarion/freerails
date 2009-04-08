/*
 * Created on 21-Jul-2005
 *
 */
package jfreerails.util;

import java.util.ArrayList;

public class List1DImpl<T> implements List1D<T> {

	private static final long serialVersionUID = 8285123045287237133L;
	private final ArrayList<T> elementData;

	public List1DImpl() {
		elementData = new ArrayList<T>();
	}

	public List1DImpl(int initialSize) {
		elementData = new ArrayList<T>();
		for (int i = 0; i < initialSize; i++) {
			elementData.add(null);
		}
	}

	public int size() {
		return elementData.size();
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

	public T get(int i) {
		return elementData.get(i);
	}

	public T removeLast() {
		int last = elementData.size() - 1;
		return elementData.remove(last);
	}

	public int add(T element) {
		elementData.add(element);
		return elementData.size() - 1;
	}

	public void set(int i, T element) {
		elementData.set(i, element);
	}

}
