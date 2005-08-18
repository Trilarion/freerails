/*
 * Created on 21-Jul-2005
 *
 */
package jfreerails.util;

import java.util.ArrayList;

public class List2DImpl<T> implements List2D<T> {

	private static final long serialVersionUID = 7614246212629595959L;
	private final ArrayList<ArrayList<T>> elementData = new ArrayList<ArrayList<T>>();
	
	public List2DImpl(int d1){		
		for(int i = 0; i < d1; i++){
			elementData.add(new ArrayList<T>());
		}		
	}

	public int sizeD1() {		
		return elementData.size();
	}

	public int sizeD2(int d1) {		
		return elementData.get(d1).size();
	}

    public T get(int d1, int d2) {
		ArrayList<T> dim2 = elementData.get(d1);
		return dim2.get(d2);		
	}

	public T removeLastD2(int d1) {
		
		
		int last = elementData.get(d1).size() -1;
		T element = elementData.get(d1).get(last);
		elementData.get(d1).remove(last);
		return element;
	}

	public int removeLastD1() {		
		int last = elementData.size() -1;
		if(sizeD2(last) != 0)
			throw new IllegalStateException(String.valueOf(last));				
		elementData.remove(last);		
		return last;	
	}

	public int addD1() {
		elementData.add(new ArrayList<T>());
		return elementData.size() -1;
	}

	public int addD2(int d1, T element) {
		ArrayList<T> d2 = elementData.get(d1);
		int index = d2.size();
		d2.add(element);
		return index;
	}

	public void set(int d1, int d2, T element) {
		elementData.get(d1).set(d2, element);		
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof List2D)) return false;						
		return Lists.equals(this, (List2D)obj);
	}

	@Override
	public int hashCode() {		
		return sizeD1();
	}

}
