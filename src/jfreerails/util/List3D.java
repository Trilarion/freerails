/*
 * Created on 21-Jul-2005
 *
 */
package jfreerails.util;

import java.io.Serializable;
import java.util.List;

public interface List3D<T> extends Serializable {
	
	int sizeD1();
	int sizeD2(int d1);
	int sizeD3(int d1, int d2);
	T get(int d1, int d2, int d3);	
	List<T> get(int d1, int d2);
	T removeLastD3(int d1, int d2);
	void removeLastD1();
	void removeLastD2(int d1);
	int addD1();
	int addD2(int d1);
	int addD3(int d1, int d2, T element);
	void set(int d1, int d2, int d3, T element);

}
